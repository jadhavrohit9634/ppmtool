package com.rjtech.ppmtool.services;

import com.rjtech.ppmtool.domain.Backlog;
import com.rjtech.ppmtool.domain.Project;
import com.rjtech.ppmtool.domain.ProjectTask;
import com.rjtech.ppmtool.exceptions.ProjectNotFoundException;
import com.rjtech.ppmtool.repositories.BacklogRepository;
import com.rjtech.ppmtool.repositories.ProjectRepository;
import com.rjtech.ppmtool.repositories.ProjectTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectTaskService {

    @Autowired
    private BacklogRepository backlogRepository;

    @Autowired
    private ProjectTaskRepository projectTaskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    public ProjectTask addProjectTask(String projectIdentifier, ProjectTask projectTask, String username) {
        //Exceptions:  Project not found

            //PTs to be added to a specific project. project != null, Backlog exists
            Backlog backlog = projectService.findProjectByIdentifier(projectIdentifier, username).getBacklog();//backlogRepository.findByProjectIdentifier(projectIdentifier);


            //set the backlog to the project task
            projectTask.setBacklog(backlog);
            //we want our project task sequence to be of the form TEST1-1, TEST1-2
            Integer BacklogSequence = backlog.getPTSequence();
            //Update the backlog sequence
            BacklogSequence++;
            backlog.setPTSequence(BacklogSequence);
            //Add sequence to project task
            projectTask.setProjectSequence(projectIdentifier + "-" + BacklogSequence);
            projectTask.setProjectIdentifier(projectIdentifier);

            //INITIAL PRIORITY when priority is null
            if (projectTask.getPriority() == null || projectTask.getPriority() == 0) {
                projectTask.setPriority(3);
            }
            //INITIAL STATUS when status is null
            if (projectTask.getStatus() == null || projectTask.getStatus() == "") {
                projectTask.setStatus("TO_DO");
            }

            return projectTaskRepository.save(projectTask);


    }

    public Iterable<ProjectTask> findBacklogById(String backlog_id, String username) {
        projectService.findProjectByIdentifier(backlog_id, username);

        return projectTaskRepository.findByProjectIdentifierOrderByPriority(backlog_id);
    }

    public ProjectTask findPTByProjectSequence(String backlog_id, String pt_id, String username){

        projectService.findProjectByIdentifier(backlog_id, username);
        //make sure that our task exist
        ProjectTask projectTask = projectTaskRepository.findByProjectSequence(pt_id);
        if(projectTask == null){
            throw new ProjectNotFoundException("Project Task with ID: '" + pt_id + "' not found");
        }

        //make sure that the backlog/project id  in the path corresponds to the right project
        if(!projectTask.getProjectIdentifier().equals(backlog_id)){
            throw new ProjectNotFoundException("Project Task with ID: '" + pt_id + "' does not exist in project: " + backlog_id);
        }
        return projectTask;
    }

    public ProjectTask updateByProjectSequence(ProjectTask updatedTask, String backlog_id, String pt_id, String username){

        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);

        projectTask = updatedTask;

        return projectTaskRepository.save(projectTask);
    }

    public void deletePTByProjectSequence(String backlog_id, String pt_id, String username){
        ProjectTask projectTask = findPTByProjectSequence(backlog_id, pt_id, username);
        projectTaskRepository.delete(projectTask);
    }

}
