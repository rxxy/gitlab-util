package com.ledaotech.base;

import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;
import org.junit.Test;
import java.util.List;
import java.util.Optional;

@Slf4j
public class GitLabApiTest {


    /**
     * 同步用户
     */
    @Test
    public void syncUsers() throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.ledaotech.com", "yXAJN38tQVUi6AhUPnyc");
        GitLabApi newGitLabApi = new GitLabApi("https://gitlab1.ledaotech.com", "ysQF9ZPfyGnZZXtLhyRV");

        deleteAllUsers();
        List<User> users = gitLabApi.getUserApi().getUsers();

        users.forEach(user -> {
            try {
                newGitLabApi.getUserApi().createUser(user, "12345678", true);
            } catch (GitLabApiException e) {
                e.printStackTrace();
            }
        });

//        users.stream().forEach(user -> {
//            try {
//                newGitLabApi.getUserApi().createUser(user, "12345678", true);
//            } catch (GitLabApiException e) {
//                e.printStackTrace();
//            }
//        });
    }


    /**
     * 同步组
     * @throws Exception
     */
    @Test
    public void syncGroup() throws Exception{
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.ledaotech.com", "yXAJN38tQVUi6AhUPnyc");

        deleteAllGroups();
        List<Group> groups = gitLabApi.getGroupApi().getGroups();
        for (Group group : groups) {
            syncGroup(group.getId());
        }
        log.info("同步group成功,同步数量:{}", groups.size());
    }

    /**
     * 同步一个group
     * @param groupId   旧仓库group id
     * @return  新仓库group id
     * @throws GitLabApiException
     */
    public Group syncGroup(Integer groupId) throws GitLabApiException {
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.ledaotech.com", "yXAJN38tQVUi6AhUPnyc");
        GitLabApi newGitLabApi = new GitLabApi("https://gitlab1.ledaotech.com", "ysQF9ZPfyGnZZXtLhyRV");
        GroupApi groupApi = gitLabApi.getGroupApi();
        GroupApi newGroupApi = newGitLabApi.getGroupApi();

        Group group = groupApi.getGroup(groupId);
        if(group.getParentId() != null){
            Group parentGroup = syncGroup(group.getParentId());
            group.setParentId(parentGroup.getId());
        }
        Optional<Group> optionalGroup = newGroupApi.getOptionalGroup(group.getFullPath());
        if(!optionalGroup.isPresent()){
            return newGroupApi.addGroup(group);
        }else {
            return optionalGroup.get();
        }
    }


    /**
     * 同步仓库代码
     * @throws Exception
     */
    @Test
    public void syncRepo() throws Exception{
        GitLabApi gitLabApi = new GitLabApi("https://gitlab.ledaotech.com", "yXAJN38tQVUi6AhUPnyc");
        GitLabApi newGitLabApi = new GitLabApi("https://gitlab1.ledaotech.com", "ysQF9ZPfyGnZZXtLhyRV");
        ProjectApi newProjectApi = newGitLabApi.getProjectApi();
        deleteAllProjects();
        List<Project> projects = gitLabApi.getProjectApi().getProjects();
        List<Namespace> namespaces = newGitLabApi.getNamespaceApi().getNamespaces();
        for (Project project : projects) {
            Namespace namespace = project.getNamespace();
            if("user".equals(namespace.getKind())){
                List<Namespace> namespaces1 = newGitLabApi.getNamespaceApi().findNamespaces(project.getNamespace().getFullPath(), 0, 1);
                project.setNamespace(namespaces1.get(0));
            }else if("group".equals(namespace.getKind())){
                Group newGroup = newGitLabApi.getGroupApi().getGroup(project.getNamespace().getFullPath());
                namespace.setId(newGroup.getId());
                project.setNamespace(namespace);
            }
            String httpUrlToRepo = project.getHttpUrlToRepo().replaceAll("//", "//rxxy:Mxcckqh123@");
            newProjectApi.createProject(project, httpUrlToRepo);
        }
    }

    /**
     * 删除所有仓库
     * @throws GitLabApiException
     */
    @Test
    public void deleteAllProjects() throws GitLabApiException {
        GitLabApi newGitLabApi = new GitLabApi("https://gitlab1.ledaotech.com", "ysQF9ZPfyGnZZXtLhyRV");
        ProjectApi projectApi = newGitLabApi.getProjectApi();
        projectApi.getProjects().forEach(project -> {
            try {
                projectApi.deleteProject(project.getId());
            } catch (GitLabApiException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 删除所有group
     * @throws GitLabApiException
     */
    @Test
    public void deleteAllGroups() throws GitLabApiException {
        GitLabApi newGitLabApi = new GitLabApi("https://gitlab1.ledaotech.com", "ysQF9ZPfyGnZZXtLhyRV");
        GroupApi groupApi = newGitLabApi.getGroupApi();
        groupApi.getGroups().forEach(group -> {
            try {
                groupApi.deleteGroup(group.getId());
                log.info("正在删除:{}", group.getPath());
            } catch (GitLabApiException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 删除所有用户
     * @throws GitLabApiException
     */
    @Test
    public void deleteAllUsers() throws GitLabApiException {
        GitLabApi newGitLabApi = new GitLabApi("https://gitlab1.ledaotech.com", "ysQF9ZPfyGnZZXtLhyRV");
        UserApi userApi = newGitLabApi.getUserApi();
        userApi.getUsers().forEach(user -> {
            try {
                if(user.getId() != 1){
                    userApi.deleteUser(user.getId());
                }
            } catch (GitLabApiException e) {
                e.printStackTrace();
            }
        });
    }

}
