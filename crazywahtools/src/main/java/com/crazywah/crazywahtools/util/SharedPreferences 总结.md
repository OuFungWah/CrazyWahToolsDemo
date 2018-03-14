# `SharedPreferences` 源码透析与总结
## 一、`SharedPreferences`的简介
`SharedPreferences`字面意思就是`共享偏好设置`，多用于存储一些开关或者是应用的某些设置信息。是Android官方提供的数据持久化方案中的一种，是一种非常轻量级的存储方案。
`SharedPreferences`有以下特点：
  * `SharedPreferences`只能存储很少量的数据
  * 同一个文件名生成的`SharedPreferences`对象都是相同的
  * `SharedPreferences`的数据写入与读取都是线程同步的，以达到数据同步。
  * `SharedPreferences`的数据保存在编程者定义好的`文件名.xml`文件中

## 二、`SharedPreferences`的源码透析
为了验证`SharedPreferences`的特点，我写了一段代码，主要是做了两件事情:
  * 新建两个使用相同文件名的`SharedPreferences`对象，判断是否为相同得到对象
  * 新建两个线程，同时对`SharedPreferences`中的数据执行读写操作，看数据的结果是如何。

```java
        final SharedPreferences sharedPreferences1 = this.getSharedPreferences("test", this.MODE_PRIVATE);
        final SharedPreferences sharedPreferences2 = this.getSharedPreferences("test", this.MODE_PRIVATE);
        Log.d(TAG, "Is that two SharedPreference is different? " + !sharedPreferences1.equals(sharedPreferences2));
        final SharedPreferences.Editor editor1 = sharedPreferences1.edit();
        final SharedPreferences.Editor editor2 = sharedPreferences2.edit();
        editor1.putInt("test", 0);
        editor1.commit();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int a = sharedPreferences1.getInt("test", 0);
                    if (a > 10) {
                        break;
                    }
                    Log.d(TAG, "This Thread A a = " + a);
                    a++;
                    editor1.putInt("test", a);
                    editor1.commit();
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    int b = sharedPreferences2.getInt("test", 0);
                    if (b > 10) {
                        break;
                    }
                    Log.d(TAG, "This Thread B b = " + b);
                    b++;
                    editor2.putInt("test", b);
                    editor2.commit();
                }
            }
        }).start();
```
以上代码运行后再控制台我们看到结果如下：
```
03-12 09:20:22.232 24184-24184/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: Is that two SharedPreference is different? false
03-12 09:20:22.239 24184-24205/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread A a = 0
03-12 09:20:22.240 24184-24206/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread B b = 1
03-12 09:20:22.243 24184-24205/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread A a = 2
03-12 09:20:22.248 24184-24206/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread B b = 3
03-12 09:20:22.251 24184-24205/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread A a = 4
03-12 09:20:22.255 24184-24206/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread B b = 5
03-12 09:20:22.259 24184-24205/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread A a = 6
03-12 09:20:22.263 24184-24206/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread B b = 7
03-12 09:20:22.268 24184-24205/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread A a = 8
03-12 09:20:22.272 24184-24206/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread B b = 9
03-12 09:20:22.276 24184-24205/com.crazywah.crazywahtoolsdemo D/SharedPreferencesTest: This Thread A a = 10

```
从结果中我们不难看到`SharedPreferences`的特点：同一个文件名生成的`SharedPreferences`对象都是相同的。从两个线程中输出的数据证实了前面提及过的一个特点---**数据的同步**。<br/>
通过源码的分析我们会知道为什么`SharedPreferences`会有这样的特点

### 读取与加载 `SharedPreferences` 保存的文件数据
在`SharedPreferences`中是怎么加载数据的呢？我们先来看以下`SharedPreferences`的构造函数：
```java
    SharedPreferencesImpl(File file, int mode) {
        mFile = file;
        //备份文件
        mBackupFile = makeBackupFile(file);
        mMode = mode;
        mLoaded = false;
        mMap = null;
        //开始加载硬盘中的数据
        startLoadFromDisk();
    }
```
在构造函数中除了一部分的变量的初始化，还有备份文件和加载数据,找到 `startLoadFromDisk()` 的源码看一下：
```Java
    private void startLoadFromDisk() {
        synchronized (mLock) {
            mLoaded = false;
        }
        new Thread("SharedPreferencesImpl-load") {
            public void run() {
                loadFromDisk();
            }
        }.start();
    }

    private void loadFromDisk() {
        ...
        Map map = null;
        StructStat stat = null;
        try {
            stat = Os.stat(mFile.getPath());
            if (mFile.canRead()) {
                BufferedInputStream str = null;
                try {
                    /* 新建输入流并指定大小限制，从这里就看出来为什么我们不能在SharedPreferences中存储过大的数据了吧 */
                    str = new BufferedInputStream(
                            new FileInputStream(mFile), 16*1024);
                    /* 从xml文件中读取数据 */
                    map = XmlUtils.readMapXml(str);
                } catch (Exception e) {
                    Log.w(TAG, "Cannot read " + mFile.getAbsolutePath(), e);
                } finally {
                    IoUtils.closeQuietly(str);
                }
            }
        } catch (ErrnoException e) {
            /* ignore */
        }

        /* 更新数据加载标志 */
        synchronized (mLock) {
            mLoaded = true;
            if (map != null) {
                mMap = map;
                mStatTimestamp = stat.st_mtime;
                mStatSize = stat.st_size;
            } else {
                mMap = new HashMap<>();
            }
            mLock.notifyAll();
        }
    }
```
从源码中看出，我们`SharedPreferences`是在新建对象的时候新建一个子线程来加载XML文件中的数据的。当然这个地方也是加上了线程同步的限制的，是为了保证数据的一致性。<br/>
在数据读取时，调用的各个get方法仅仅只是访问了已加载好在内存中的 `SharedPreferences` 对象中的 Map 的数据。
### 修改 `SharedPreferences` 中数据的重要类 `Editor`
需要修改 `SharedPreferences` 数据时，我们需要先通过  `SharedPreferences` 的 `edit()` 方法获取 Editor 的对象，然后调用`put`方法修改我们需要修改的数据，最后在调用 `commit()` 将数据提交至存储中。
#### `Editor` 的 `commit()` 方法
我们都知道在调用`commit()`以前，你作的所有修改都是无效的。那么`commit`到底做了什么呢？
1. 将修改提交至内存
  1. 如果当前有多个线程在等待中，先给 map 做一个克隆备份，避免影响其他线程的数据
  2. 如果Editor执行了clear()，清空Map
  3. 将本次提交的修改的数据 mModified 汇总到当前的总 Map mMap 中
  4. 如果有注册监听器，将本次修改的所有 Key 储存到列表中
  5. 更新内存版本号
  6. 释放线程锁
  7. 返回提交结果
  ```java
          // 如果有作过修改，changeMode = true
          private MemoryCommitResult commitToMemory() {
              //内存版本号
              long memoryStateGeneration;
              //本次修改中改动过的 Key 列表
              List<String> keysModified = null;
              //注册的监听者列表
              Set<OnSharedPreferenceChangeListener> listeners = null;
              //即将写入硬盘的Map
              Map<String, Object> mapToWriteToDisk;

              //上线程锁。保证多线程的同步性
              synchronized (SharedPreferencesImpl.this.mLock) {
                  if (mDiskWritesInFlight > 0) {
                      /* 如果当前有多个线程在等待中，先给 map 做一个克隆备份，避免影响其他线程的数据 */
                      mMap = new HashMap<String, Object>(mMap);
                  }
                  mapToWriteToDisk = mMap;
                  mDiskWritesInFlight++;

                  boolean hasListeners = mListeners.size() > 0;
                  if (hasListeners) {
                      keysModified = new ArrayList<String>();
                      listeners = new HashSet<OnSharedPreferenceChangeListener>(mListeners.keySet());
                  }

                  // 第二层线程锁
                  synchronized (mLock) {
                      boolean changesMade = false;

                      /* 如果Editor执行了clear()，清空Map */
                      if (mClear) {
                          if (!mMap.isEmpty()) {
                              changesMade = true;
                              mMap.clear();
                          }
                          mClear = false;
                      }

                      /* 将本次提交的修改的数据 mModified 汇总到当前的总 Map mMap 中 */
                      for (Map.Entry<String, Object> e : mModified.entrySet()) {
                          String k = e.getKey();
                          Object v = e.getValue();
                          if (v == this || v == null) {
                              if (!mMap.containsKey(k)) {
                                  continue;
                              }
                              mMap.remove(k);
                          } else {
                              if (mMap.containsKey(k)) {
                                  Object existingValue = mMap.get(k);
                                  if (existingValue != null && existingValue.equals(v)) {
                                      continue;
                                  }
                              }
                              mMap.put(k, v);
                          }
                          changesMade = true;
                          /* 如果有注册监听器，将本次修改的所有 Key 储存到列表中 */
                          if (hasListeners) {
                              keysModified.add(k);
                          }
                      }
                      //清空修改列表
                      mModified.clear();
                      /* 更新内存版本号 */
                      if (changesMade) {
                          mCurrentMemoryStateGeneration++;
                      }
                      memoryStateGeneration = mCurrentMemoryStateGeneration;
                  }
              }
              /* 释放线程锁 */
              return new MemoryCommitResult(memoryStateGeneration, keysModified, listeners,
                      mapToWriteToDisk);
          }
  ```
2. 将写入文件的任务线程提交到队列中
  1. 同步锁
  2. 备份原文件
  3. 如果当前数据版本低于commit中更新的数据时则需要输入
  4. 写入XML文件
  5. 同步写入
  6. 关闭输入流
  7. 若写入成功则删除备份文件
  ```java
    private void enqueueDiskWrite(final MemoryCommitResult mcr,
                                  final Runnable postWriteRunnable) {
        final boolean isFromSyncCommit = (postWriteRunnable == null);

        final Runnable writeToDiskRunnable = new Runnable() {
                public void run() {
                    synchronized (mWritingToDiskLock) {
                        /*写入文件操作*/
                        writeToFile(mcr, isFromSyncCommit);
                    }
                    synchronized (mLock) {
                        //任务完成，减去竞争者
                        mDiskWritesInFlight--;
                    }
                    if (postWriteRunnable != null) {
                        postWriteRunnable.run();
                    }
                }
            };
        //将新建的线程加入到执行队列当中去
        QueuedWork.queue(writeToDiskRunnable, !isFromSyncCommit);
    }

        // Note: must hold mWritingToDiskLock
        private void writeToFile(MemoryCommitResult mcr, boolean isFromSyncCommit) {

            ...

            /* 对文件进行备份 */
            if (fileExists) {
                boolean needsWrite = false;
                if (mDiskStateGeneration < mcr.memoryStateGeneration) {
                    if (isFromSyncCommit) {
                        needsWrite = true;
                    } else {
                        synchronized (mLock) {
                            if (mCurrentMemoryStateGeneration == mcr.memoryStateGeneration) {
                                needsWrite = true;
                            }
                        }
                    }
                }

                if (!needsWrite) {
                    mcr.setDiskWriteResult(false, true);
                    return;
                }

                boolean backupFileExists = mBackupFile.exists();

                    if (DEBUG) {
                        backupExistsTime = System.currentTimeMillis();
                    }

                if (!backupFileExists) {
                    if (!mFile.renameTo(mBackupFile)) {
                        Log.e(TAG, "Couldn't rename file " + mFile
                              + " to backup file " + mBackupFile);
                        mcr.setDiskWriteResult(false, false);
                        return;
                    }
                } else {
                    mFile.delete();
                }
            }

            try {
                /* 新建文件输出流 */
                FileOutputStream str = createFileOutputStream(mFile);

                ...

                /* 将Map中的数据存储到XML文件中 */
                XmlUtils.writeMapXml(mcr.mapToWriteToDisk, str);
                /* 同步写入结果，若成功则删除备份 */
                FileUtils.sync(str);
                str.close();
                ContextImpl.setFilePermissionsFromMode(mFile.getPath(), mMode, 0);

                ...

                mBackupFile.delete();

                ...

                mcr.setDiskWriteResult(true, true);

                ...

            /* 释放信号量 */
            mcr.setDiskWriteResult(false, false);
        }    
  ```
3. 抢占信号量（此信号量会在写入文件操作完成后释放，不管写入成功与否）
4. 向监听者发送数据更新消息


## 三、`SharedPreferences`的使用
### 1、 获取`Editor`
调用 edit() 以获取 SharedPreferences.Editor。
### 2、使用 `putBoolean()` 和 `putString()` 等方法添加值。
### 3、使用 commit() 提交新值
### 4、要读取值，请使用 getBoolean() 和 getString() 等 SharedPreferences 方法。
