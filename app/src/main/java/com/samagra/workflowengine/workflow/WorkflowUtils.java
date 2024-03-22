package com.samagra.workflowengine.workflow;

import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.commons.MetaDataExtensions;
import com.samagra.commons.models.metadata.CompetencyModel;
import com.samagra.commons.models.chaptersdata.ChapterMapping;
import com.samagra.workflowengine.workflow.model.FlowConfig;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmList;

public class WorkflowUtils {

    public static RealmList<String> getRefIdList(String subject, int grade, List<ChapterMapping> chapterMappings, String competencyID, String type, CommonsPrefsHelperImpl prefs) {
        if (chapterMappings == null || chapterMappings.size() == 0 || competencyID == null || type == null) {
            return null;
        }
        ChapterMapping selectedMapping = null;
        for (ChapterMapping mapping : chapterMappings) {
            String subjectFromId = MetaDataExtensions.getSubjectFromId(mapping.getSubjectId(), prefs.getSubjectsListJson());
            if (mapping.getGrade() == grade &&  subjectFromId.equalsIgnoreCase(subject)
                    && mapping.getCompetencyId().equalsIgnoreCase(competencyID)
                    && mapping.getType().equalsIgnoreCase(type)) {
                selectedMapping = mapping;
                break;
            }
        }
        if (selectedMapping == null || selectedMapping.getRefIds() == null || selectedMapping.getRefIds().size() == 0) {
            return null;
        }
        return selectedMapping.getRefIds();
    }

    public static ArrayList<FlowConfig> getWorkflowConfigForCompetencies(List<CompetencyModel> competencyList, CommonsPrefsHelperImpl prefs) {
        ArrayList<FlowConfig> flowConfigList = new ArrayList<>();
        if (competencyList != null) {
            for (CompetencyModel competency : competencyList) {
                FlowConfig flowConfig = new FlowConfig();
                flowConfig.setGradeNumber(competency.getGrade());
                String subject = MetaDataExtensions.INSTANCE.getSubjectFromId(competency.getSubjectId(), prefs.getSubjectsListJson());
                flowConfig.setSubject(subject);
                flowConfig.setCompetencyId(competency.getCId() + "");
                List<Integer> states = new ArrayList<>();
                states.add(competency.getFlowState());
                flowConfig.setStates(states);
                flowConfigList.add(flowConfig);
            }
        }
        return flowConfigList;
    }
}
