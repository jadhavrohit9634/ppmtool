package com.rjtech.ppmtool.services;

import com.rjtech.ppmtool.domain.Backlog;
import com.rjtech.ppmtool.domain.Project;
import com.rjtech.ppmtool.domain.User;
import com.rjtech.ppmtool.exceptions.ProjectIdException;
import com.rjtech.ppmtool.exceptions.ProjectNotFoundException;
import com.rjtech.ppmtool.repositories.BacklogRepository;
import com.rjtech.ppmtool.repositories.ProjectRepository;
import com.rjtech.ppmtool.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private UserRepository userRepository;

    public Project saveProject(Project project, String username) {
        //Business Logic will come here

        //Update Project scenarios
        //1. project.getId != null
        //2. find in db by id --> which can return null
        if(project.getId() != null){
            Project existingProject = projectRepository.findByProjectIdentifier(project.getProjectIdentifier().toUpperCase());
            if(existingProject != null && (!existingProject.getProjectLeader().equals(username))){
                throw new ProjectNotFoundException("Project not found in your account");
            }else if (existingProject == null){
                throw new ProjectNotFoundException("Project with ID: '" + project.getProjectIdentifier() + "' cannot be updated because it doesn't exist.");
            }
        }

        try {

            User user = userRepository.findByUsername(username);

            project.setUser(user);
            project.setProjectLeader(user.getUsername());
            String projectIdentifier = project.getProjectIdentifier().toUpperCase();
            project.setProjectIdentifier(projectIdentifier);

            if(project.getId() == null) {
                Backlog backlog = new Backlog();
                project.setBacklog(backlog);
                backlog.setProject(project);
                backlog.setProjectIdentifier(projectIdentifier);
            }

            if(project.getId() != null){
                project.setBacklog(backlogRepository.findByProjectIdentifier(projectIdentifier));
            }
            return projectRepository.save(project);
        } catch (Exception e){
            throw new ProjectIdException("Project ID '" + project.getProjectIdentifier().toUpperCase() + "' already exist");
        }
    }

    public Project  findProjectByIdentifier(String projectId, String username) {
        Project project = projectRepository.findByProjectIdentifier(projectId.toUpperCase());
        if(project == null) {
            throw  new ProjectIdException("Project ID '" + projectId.toUpperCase() + "' does not exist");
        }

        if(!project.getProjectLeader().equals(username)){
            throw new ProjectNotFoundException("Project not found in your account");
        }

        return project;
    }

    public Iterable<Project> findAllProjects(String username) {
        return projectRepository.findAllByProjectLeader(username);
    }

    public void deleteProjectByIdentifier(String projectId, String username){

            projectRepository.delete(findProjectByIdentifier(projectId, username));

    }

    public Project updateProject(Project updatedProject){
        updatedProject.setProjectIdentifier(updatedProject.getProjectIdentifier().toUpperCase());

        Project oldProject = projectRepository.findByProjectIdentifier(updatedProject.getProjectIdentifier());

        if(oldProject == null){
            throw  new ProjectIdException("Cannot update project with ID '" + updatedProject.getProjectIdentifier().toUpperCase() + "'. This project does not exist");
        }
        updatedProject.setId(oldProject.getId());
        return projectRepository.save(updatedProject);
    }
}
