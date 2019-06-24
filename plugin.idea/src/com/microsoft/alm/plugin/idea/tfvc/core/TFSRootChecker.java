package com.microsoft.alm.plugin.idea.tfvc.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vcs.LocalFilePath;
import com.intellij.openapi.vcs.VcsKey;
import com.intellij.openapi.vcs.VcsRootChecker;
import com.microsoft.alm.plugin.external.models.Workspace;
import com.microsoft.alm.plugin.external.utils.CommandUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TFSRootChecker extends VcsRootChecker {

    private static Logger logger = Logger.getInstance(TFSRootChecker.class);

    @Override
    public boolean isRoot(@NotNull String path) {
        try {
            logger.info("Request for isRoot: " + path);
            path = new File(path).getCanonicalPath();

            Boolean status = isUnderTFVCFromCache(path);
            if (status != null) {
                logger.info(path + " served from cache");
                return status;
            }

            Workspace workspace = CommandUtils.tryGetPartialWorkspace(path);
            if (workspace == null) {
                putCachedResult(path, CacheCheckResult.NotUnderTFVC);
                return false;
            }

            boolean result = false;
            for (Workspace.Mapping mapping : workspace.getMappings()) {
                String mappingPath = new File(mapping.getLocalPath()).getCanonicalPath();
                putCachedResult(mappingPath, CacheCheckResult.MappingRoot);
                if (path.equals(mappingPath)) {
                    result = true;
                }
            }

            return result;
        } catch (IOException ex) {
            logger.error(ex);
            return false;
        }
    }

    @NotNull
    @Override
    public VcsKey getSupportedVcs() {
        return TFSVcs.getKey();
    }

    @Override
    public boolean isVcsDir(@NotNull String path) {
        return path.equalsIgnoreCase("$tf");
    }

    private enum CacheCheckResult {
        /**
         * Path is a TFVC mapping root.
         */
        MappingRoot,
        /**
         * Path is not a TFVC mapping root and is not under any.
         */
        NotUnderTFVC
    }

    private final Map<String, CacheCheckResult> checkedRoots = new HashMap<String, CacheCheckResult>();

    /**
     * @return true if path is TFVC root; false if path is not a TFVC root; null if not able to determine from cache.
     */
    @Nullable
    private Boolean isUnderTFVCFromCache(@NotNull String path) {
        synchronized (checkedRoots) {
            CacheCheckResult status = checkedRoots.get(path);
            if (status != null) {
                // If the result is cached then quickly return it.
                return status == CacheCheckResult.MappingRoot;
            }

            LocalFilePath pathToCheck = new LocalFilePath(path, true);
            for (Map.Entry<String, CacheCheckResult> entry : checkedRoots.entrySet()) {
                LocalFilePath entryPath = new LocalFilePath(entry.getKey(), true);
                if (entry.getValue() == CacheCheckResult.MappingRoot && pathToCheck.isUnder(entryPath, true)) {
                    // If a path to check is under any of registered roots, then return false, because it cannot be root
                    // itself
                    return false;
                }

                if (entry.getValue() == CacheCheckResult.NotUnderTFVC && entryPath.isUnder(pathToCheck, true)) {
                    // If a path to check is above any of the paths we've already checked and determined to not be TFVC
                    // roots, then return false, because it cannot be a root then
                    return false;
                }
            }

            return null; // cannot determine from cache
        }
    }

    private void putCachedResult(@NotNull String path, CacheCheckResult result) {
        synchronized (checkedRoots) {
            checkedRoots.put(path, result);
        }
    }
}
