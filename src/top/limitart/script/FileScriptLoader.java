/*
 * Copyright (c) 2016-present The Limitart Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.limitart.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.limitart.base.Conditions;
import top.limitart.concurrent.NamedThreadFactory;
import top.limitart.util.CodecUtil;
import top.limitart.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 源码脚本加载器
 *
 * @param <KEY>
 * @author hank
 */
public class FileScriptLoader<KEY> extends AbstractScriptLoader<KEY> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileScriptLoader.class);
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory() {
        @Override
        public String namePrefix() {
            return "file-script-loader-scheduler";
        }
    });
    // 脚本地址根目录
    private final String scriptRootPath;

    /**
     * 初始化
     *
     * @param scriptRootPath     脚本地址根目录
     * @param autoReloadInterval 自动重加载间隔(秒)
     * @throws IOException
     * @throws ScriptConstructException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ScriptKeyDuplicatedException
     * @throws ScriptNotExistException
     * @throws NoSuchAlgorithmException
     */
    public FileScriptLoader(String scriptRootPath, int autoReloadInterval)
            throws IOException, InstantiationException, IllegalAccessException, ScriptConstructException,
            NoSuchAlgorithmException, ScriptNotExistException, ScriptKeyDuplicatedException {
        this.scriptRootPath = scriptRootPath;
        if (autoReloadInterval > 0) {
            worker.scheduleAtFixedRate(() -> {
                try {
                    reloadAll0();
                } catch (InstantiationException | IllegalAccessException | NoSuchAlgorithmException | IOException
                        | ScriptConstructException | ScriptNotExistException | ScriptKeyDuplicatedException e) {
                    LOGGER.error("load scripts error!", e);
                }
            }, 0, autoReloadInterval, TimeUnit.SECONDS);
        } else {
            reloadAll0();
        }
    }

    /**
     * 初始化
     *
     * @param scriptRootPath 脚本地址根目录
     * @throws IOException
     * @throws ScriptConstructException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ScriptKeyDuplicatedException
     * @throws ScriptNotExistException
     * @throws NoSuchAlgorithmException
     */
    public FileScriptLoader(String scriptRootPath) throws IOException, InstantiationException, IllegalAccessException,
            ScriptConstructException, NoSuchAlgorithmException, ScriptNotExistException, ScriptKeyDuplicatedException {
        this(scriptRootPath, 0);
    }

    /**
     * 重载全部脚本
     */
    public void reloadAll() {
        worker.execute(() -> {
            try {
                reloadAll0();
            } catch (InstantiationException | IllegalAccessException | NoSuchAlgorithmException | IOException
                    | ScriptConstructException | ScriptNotExistException | ScriptKeyDuplicatedException e) {
                LOGGER.error("reload all error", e);
            }
        });
    }

    /**
     * 重载全部脚本
     *
     * @throws IOException
     * @throws ScriptConstructException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchAlgorithmException
     * @throws ScriptNotExistException
     * @throws ScriptKeyDuplicatedException
     */
    private void reloadAll0() throws IOException, ScriptConstructException, InstantiationException,
            IllegalAccessException, NoSuchAlgorithmException, ScriptNotExistException, ScriptKeyDuplicatedException {
        File dir_root = new File(scriptRootPath);
        if (!dir_root.exists()) {
            throw new IOException("scripts root dir does not exist:" + scriptRootPath);
        }
        if (!dir_root.isDirectory()) {
            throw new IOException("file is not dir:" + scriptRootPath);
        }
        ByteCodeClassLoader loader = new ByteCodeClassLoader();
        List<File> result = FileUtil.getFiles(dir_root, "java");
        for (File file : result) {
            // 是否是老文件
            KEY scriptKey = getScriptKey(file);
            // 新脚本
            if (scriptKey == null) {
                byte[] readFile1 = FileUtil.readFile1(file);
                Class<?> parseClass = loader.parseClass(file.toURI(), readFile1);
                Object newInstance = parseClass.newInstance();
                if (!(newInstance instanceof Script)) {
                    throw new ScriptConstructException(parseClass);
                }
                @SuppressWarnings("unchecked")
                Script<KEY> script = (Script<KEY>) newInstance;
                registerScriptData(script, CodecUtil.toMD5(readFile1), getFilePath(file));
            } else {
                reloadScript0(loader, scriptKey);
            }
        }
    }

    /**
     * 重载指定脚本
     *
     * @param scriptId
     */
    public void reloadScript(KEY scriptId) {
        worker.execute(() -> {
            try {
                reloadScript0(new ByteCodeClassLoader(), scriptId);
            } catch (InstantiationException | IllegalAccessException | NoSuchAlgorithmException | IOException
                    | ScriptNotExistException | ScriptConstructException | ScriptKeyDuplicatedException e) {
                LOGGER.error("reload error:" + scriptId, e);
            }
        });
    }

    /**
     * 重载脚本
     *
     * @param scriptId
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchAlgorithmException
     * @throws ScriptNotExistException
     * @throws ScriptConstructException
     * @throws ScriptKeyDuplicatedException
     */
    private void reloadScript0(ByteCodeClassLoader loader, KEY scriptId)
            throws IOException, InstantiationException, IllegalAccessException, NoSuchAlgorithmException,
            ScriptNotExistException, ScriptConstructException, ScriptKeyDuplicatedException {
        Conditions.notNull(scriptId, "scriptId");
        String filePath = getFilePath(scriptId);
        File file = new File(filePath);
        byte[] readFile1 = FileUtil.readFile1(file);
        String md5Encode32 = CodecUtil.toMD5(readFile1);
        if (isSameCode(scriptId, md5Encode32)) {
            return;
        }
        Class<?> parseClass = loader.parseClass(file.toURI(), readFile1);
        Object newInstance = parseClass.newInstance();
        if (!(newInstance instanceof Script)) {
            throw new ScriptConstructException(parseClass);
        }
        @SuppressWarnings("unchecked")
        Script<KEY> newScript = (Script<KEY>) newInstance;
        registerScriptData(newScript, md5Encode32, getFilePath(file));
    }
}
