package io.choerodon.devops.infra.util;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.annotation.Nullable;

import io.choerodon.devops.api.vo.kubernetes.C7nHelmRelease;
import io.choerodon.devops.infra.dto.DevopsEnvFileResourceDTO;
import io.choerodon.devops.infra.enums.C7NHelmReleaseMetadataType;
import io.choerodon.devops.infra.enums.EnvironmentType;
import io.choerodon.devops.infra.enums.GitOpsObjectError;
import io.choerodon.devops.infra.enums.ResourceType;
import io.choerodon.devops.infra.exception.GitOpsExplainException;

/**
 * GitOps解析相关的工具类
 *
 * @author zmf
 * @since 11/4/19
 */
public class GitOpsUtil {
    private GitOpsUtil() {
    }

    /**
     * 实例是否是集群组件的实例
     *
     * @param envType        环境类型
     * @param c7nHelmRelease release数据
     * @return true则是，反之，不是
     */
    public static boolean isClusterComponent(String envType, C7nHelmRelease c7nHelmRelease) {
        return EnvironmentType.SYSTEM.getValue().equals(envType) && C7NHelmReleaseMetadataType.CLUSTER_COMPONENT.getType().equals(c7nHelmRelease.getMetadata().getType());
    }

    /**
     * 根据资源名称对资源进行分拣处理，从所有涉及的资源分拣出哪些是新增的，更新的和删除的
     *
     * @param beforeResourceNames 此处操作前数据库的所有资源名称
     * @param all                 所有涉及的资源，分类之后这个列表中存放的是待删除的资源
     * @param add                 放置新增的资源的容器，分类之后将需要更新的资源放入此处，建议传入时为空
     * @param update              放置更新的资源的容器，分类之后将需要更新的资源放入此处，建议传入时为空
     * @param getName             获取资源的名称的逻辑
     */
    public static <T> void pickCUDResource(List<String> beforeResourceNames,
                                           List<T> all,
                                           List<T> add,
                                           List<T> update,
                                           Function<T, String> getName) {
        Iterator<T> iterator = all.iterator();
        while (iterator.hasNext()) {
            T obj = iterator.next();
            if (beforeResourceNames.contains(getName.apply(obj))) {
                update.add(obj);
                iterator.remove();
            } else {
                add.add(obj);
            }
        }
    }

    /**
     * 是否被当前的操作删除了
     *
     * @param beforeSyncDelete 当前操作删除的文件及其对应关系
     * @param resourceId       待判断的资源id
     * @param resourceType     资源类型
     * @return true表示被删除了，反之，没有被删除
     */
    public static boolean isDeletedByCurrentOperation(List<DevopsEnvFileResourceDTO> beforeSyncDelete,
                                                      Long resourceId, ResourceType resourceType) {
        return beforeSyncDelete.stream().anyMatch(
                envFileResourceDTO -> envFileResourceDTO.getResourceType().equals(resourceType.getType())
                        && envFileResourceDTO.getResourceId().equals(resourceId));
    }

    /**
     * 通过对象的某一个key的比较，来确认list中是否包含某个对象
     *
     * @param beforeList        已有的list
     * @param toBeJudged        待判断的对象
     * @param propertyExtractor 提取对象的用于比较的数据的提取器
     * @param <T>               泛型参数
     * @return true表示包含
     */
    public static <T> boolean isContainedByList(
            List<T> beforeList, T toBeJudged, Function<T, Object> propertyExtractor) {
        return beforeList.stream().anyMatch(before -> identifyByProperty(before, toBeJudged, propertyExtractor));
    }

    /**
     * 通过对象的某一个key比较对象是否相等
     *
     * @param one               一个对象
     * @param another           另一个对象
     * @param propertyExtractor 提取对象的用于比较的数据的提取器
     * @param <T>               泛型参数
     * @return true表示相等
     */
    public static <T> boolean identifyByProperty(T one, T another, Function<T, Object> propertyExtractor) {
        return Objects.equals(propertyExtractor.apply(one), propertyExtractor.apply(another));
    }

    /**
     * 校验资源未在数据库中定义，已定义则抛异常
     *
     * @param devopsEnvFileResourceDTO 资源所对应的数据纪录
     * @param filePath                 文件路径
     * @param resourceName             资源名称
     */
    public static void checkNotExistInDb(@Nullable DevopsEnvFileResourceDTO devopsEnvFileResourceDTO,
                                         String filePath, String resourceName) {
        if (devopsEnvFileResourceDTO != null &&
                !devopsEnvFileResourceDTO.getFilePath().equals(filePath)) {
            throwExistEx(filePath, resourceName);
        }
    }

    /**
     * 抛出资源对象存在的异常
     *
     * @param filePath     资源所在文件路径
     * @param resourceName 资源名称
     */
    public static void throwExistEx(String filePath, String resourceName) {
        throw new GitOpsExplainException(
                GitOpsObjectError.OBJECT_EXIST.getError(), filePath, resourceName);
    }
}
