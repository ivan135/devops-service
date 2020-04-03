package io.choerodon.devops.infra.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.util.StringUtils
import spock.lang.Specification

import io.choerodon.devops.infra.dto.gitlab.ci.CiJob
import io.choerodon.devops.infra.dto.gitlab.ci.GitlabCi
import io.choerodon.devops.infra.dto.gitlab.ci.OnlyExceptPolicy

/**
 *
 * @author zmf
 * @since 20-4-2
 *
 */
class GitlabCiUtilSpec extends Specification {
    def "GitlabCi2yaml"() {
        given: "准备测试数据"
        String json = "{\"include\":[{\"local\":\"/url.yaml\"},{\"remote\":\"https://sdf.com/ci.yaml\"}],\"image\":\"registry.cn-shanghai.aliyuncs.com/c7n/cibase:0.9.1\",\"stages\":[\"build\",\"release\"],\"build backend\":{\"stage\":\"build\",\"only\":{\"refs\":[\"master\"]},\"script\":[\"ls -al\",\"rm -rf ./*\"],\"except\":{\"refs\":[\"master\"]},\"cache\":{\"key\":{\"files\":[\"app.jar\"]},\"paths\":[\"./name.jar\",\"./app.jar\"],\"untracked\":true}},\"release backend\":{\"stage\":\"build\",\"only\":{\"refs\":[\"master\"]},\"script\":[\"ls -al\",\"rm -rf ./*\"],\"except\":{\"refs\":[\"master\"]},\"cache\":{\"key\":{\"files\":[\"app.jar\"]},\"paths\":[\"./name.jar\",\"./app.jar\"],\"untracked\":true}}}"
        GitlabCi gitlabCi = new ObjectMapper().readValue(json, GitlabCi)
        gitlabCi.setBeforeScript(["http_status_code=`curl -o .auto_devops.sh -s -m 10 --connect-timeout 10 -w %{http_code} \"\${CHOERODON_URL}/devops/ci?token=\${Token}&type=microservice\"`\n" +
                                          "if [ \"\$http_status_code\" != \"200\" ]; then\n" +
                                          "  cat ./.auto_devops.sh\n" +
                                          "  exit 1\n" +
                                          "fi\n" +
                                          "source ./.auto_devops.sh\n" +
                                          "function docker_build(){\n" +
                                          "    docker build -t \${DOCKER_REGISTRY}/\${GROUP_NAME}/\${PROJECT_NAME}:\${CI_COMMIT_TAG} .\n" +
                                          "    docker login -u \${DOCKER_USER} -p \${DOCKER_PWD} \${DOCKER_REGISTRY}\n" +
                                          "    docker push \${DOCKER_REGISTRY}/\${GROUP_NAME}/\${PROJECT_NAME}:\${CI_COMMIT_TAG}\n" +
                                          "}"])

        when: "调用转为yaml的方法"
        String yaml = GitlabCiUtil.gitlabCi2yaml(gitlabCi)
        println(yaml)

        then: "校验结果"
        !StringUtils.isEmpty(yaml)
        noExceptionThrown()
    }

    def "GitlabCi2yaml2"() {
        given: "准备测试数据"
        GitlabCi gitlabCi = new GitlabCi()
        gitlabCi.setImage("registry.cn-hangzhou.aliyuncs.com/choerodon-tools/golang-ci:0.8.0")
        gitlabCi.setBeforeScript(["http_status_code=`curl -o .auto_devops.sh -s -m 10 --connect-timeout 10 -w %{http_code} \"\${CHOERODON_URL}/devops/ci?token=\${Token}&type=microservice\"`\n" +
                                          "if [ \"\$http_status_code\" != \"200\" ]; then\n" +
                                          "  cat ./.auto_devops.sh\n" +
                                          "  exit 1\n" +
                                          "fi\n" +
                                          "source ./.auto_devops.sh\n" +
                                          "function docker_build(){\n" +
                                          "    docker build -t \${DOCKER_REGISTRY}/\${GROUP_NAME}/\${PROJECT_NAME}:\${CI_COMMIT_TAG} .\n" +
                                          "    docker login -u \${DOCKER_USER} -p \${DOCKER_PWD} \${DOCKER_REGISTRY}\n" +
                                          "    docker push \${DOCKER_REGISTRY}/\${GROUP_NAME}/\${PROJECT_NAME}:\${CI_COMMIT_TAG}\n" +
                                          "}"])
        gitlabCi.setStages(["docker-build", "test zmf"])
        CiJob build = new CiJob()
        build.setStage("docker-build")
        build.setScript(["docker_build", "chart_build"])
        build.setExcept(new OnlyExceptPolicy(["master", "/.*zmf.*/i"], null, null))
        CiJob test = new CiJob()
        test.setStage("test zmf")
        test.setScript(["echo 'zmf-test-gitlab-ci-file'"])
        test.setOnly(new OnlyExceptPolicy(["master", "/.*zmf.*/i"], null, null))
        Map<String, CiJob> jobs = new LinkedHashMap<>()
        jobs.put("docker-build", build)
        jobs.put("test zmf", test)
        gitlabCi.setJobs(jobs)


        when: "调用转为yaml的方法"
        String yaml = GitlabCiUtil.gitlabCi2yaml(gitlabCi)
        println(yaml)

        then: "校验结果"
        !StringUtils.isEmpty(yaml)
        noExceptionThrown()
    }
}