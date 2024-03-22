package com.samagra.workflowengine.workflow;

import static com.samagra.commons.utils.CommonConstants.BOLO;
import static com.samagra.commons.utils.CommonConstants.ODK;

import android.content.Context;
import android.widget.Toast;

import com.google.gson.Gson;
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl;
import com.samagra.commons.models.schoolsresponsedata.SchoolsData;
import com.samagra.parent.UtilityFunctions;
import com.samagra.workflowengine.WorkflowProperty;
import com.samagra.workflowengine.bolo.ReadAlongProperties;
import com.samagra.workflowengine.bolo.ReadAlongWorkflow;
import com.samagra.workflowengine.odk.OdkProperties;
import com.samagra.workflowengine.odk.OdkWorkflow;
import com.samagra.workflowengine.workflow.model.Action;
import com.samagra.commons.models.chaptersdata.ChapterMapping;
import com.samagra.workflowengine.workflow.model.FlowConfig;
import com.samagra.workflowengine.workflow.model.State;
import com.samagra.workflowengine.workflow.model.WorkflowConfig;
import com.samagra.workflowengine.workflow.model.stateresult.AssessmentStateResult;
import com.samagra.workflowengine.workflow.model.stateresult.StateResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;
import io.realm.RealmList;

public class WorkflowManager {
    private static WorkflowManager instance;
    private final LinkedList<State> states;
    private final LinkedList<State> originalStates;
    private WorkflowConfig workflowConfig;
    private Map<Long, State> stateMap;
    private Map<Integer, Action> actionMap;
    private WorkflowListener listener;
    private State currentState;
    private int trialIndex = 0;
    private int currentStateCount = 0;
    private final Map<String, Object> runtimeProperties;
    private List<StateResult> stateResults;
    private String type;
    private Map<String, String> displayValuesMapper;

    private WorkflowManager() {
        states = new LinkedList<>();
        originalStates = new LinkedList<>();
        runtimeProperties = new HashMap<>();
        stateResults = new ArrayList<>();
    }

    public static synchronized WorkflowManager getInstance() {
        if (instance == null) {
            instance = new WorkflowManager();
        }
        return instance;
    }

    public static void setWorkflowManagerAsNull() {
        instance = null;
    }

    public void loadConfig(String json) {
        workflowConfig = new Gson().fromJson(json, WorkflowConfig.class);
        stateMap = new HashMap<>();
        actionMap = new HashMap<>();
        for (State state : workflowConfig.getStates()) {
            stateMap.put(state.getId(), state);
        }
        for (Action action : workflowConfig.getActions()) {
            actionMap.put(action.getId(), action);
        }
    }

    public void startWorkflow(Context context, int grade, String subject, WorkflowResultListener listener) {
        start(grade, subject, new WorkflowManager.WorkflowListener() {
            @Override
            public void processState(LinkedList<State> states, State state, WorkflowModuleCallback callback) {
                StringBuilder sB = new StringBuilder();
                int i = 0;
                for (State s : states) {
                    if (s != null) {
                        sB.append("Index : " + ++i).append(", State Id : ").append(s.getId()).append(" - ").append(s.getType()).append("\n");
                    }else{
                        Timber.e("Something went wrong on States.");
                    }
                }
//                streamTv.setText(sB + "Warning Counter : " + WorkflowManager.getInstance().trialIndex);
                Timber.i(sB + "Warning Counter : " + WorkflowManager.getInstance().trialIndex);
                Timber.i("State Details : " + state.toString());
                type = state.getType();
                CommonsPrefsHelperImpl prefs = new CommonsPrefsHelperImpl(context, "prefs");
                switch (state.getType()) {
                    case BOLO:
//                        context.startActivity(new Intent(context, StudentHomeActivity.class));
                        ReadAlongWorkflow readAlongWorkflow = new ReadAlongWorkflow(callback);
                        ReadAlongProperties readAlongProperties = new ReadAlongProperties();
                        readAlongProperties.setGrade((Integer) runtimeProperties.get("grade"));
                        readAlongProperties.setSubject((String) runtimeProperties.get("subject"));
                        readAlongProperties.setStudentCount((Integer) runtimeProperties.get(WorkflowProperty.CURRENT_STUDENT_COUNT));
                        readAlongProperties.setSchoolData((SchoolsData) runtimeProperties.get(WorkflowProperty.SCHOOL_DATA));
                        readAlongProperties.setCompetencyName((String) runtimeProperties.get(WorkflowProperty.SELECTED_COMPETENCY));
//                        readAlongProperties.setStudent((String) runtimeProperties.get(WorkflowProperty.STUDENT));
                        //todo check by shashank
                        readAlongProperties.setRequiredWords(state.getStateData().successCriteria);
                        String competencyID = (String) runtimeProperties.get(WorkflowProperty.COMPETENCY_ID);
                        readAlongProperties.setCompetencyId(competencyID);
                        ArrayList<ChapterMapping> chapterMappingList = (ArrayList<ChapterMapping>) runtimeProperties.get(WorkflowProperty.CHAPTER_MAPPING_LIST);
                        RealmList<String> refIdList = WorkflowUtils.getRefIdList(subject, (Integer) runtimeProperties.get("grade"),
                                chapterMappingList, competencyID, type, prefs);
                        Timber.d("Bolo competency id, reference Ids : " + competencyID + "  new id  " + refIdList);
                        if (refIdList==null){
                            onComplete();
                            break;
                        }
                        readAlongProperties.setBookIdList(new ArrayList<>(refIdList));
                        readAlongProperties.setStateGrade(state.getGradeNumber());
                        readAlongProperties.setStartTime(readAlongWorkflow.startTime);
                        if (state.getDecision().getMeta() != null) {
                            try {
                                readAlongProperties.setCheckFluency(!state.getDecision().getMeta().getAsJsonObject().get("testType").getAsString().equalsIgnoreCase("Count"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Timber.i("Bolo Module Started with " + readAlongProperties);
                        readAlongWorkflow.setProps(readAlongProperties);
                        if (context != null) {
                            readAlongWorkflow.onStart(context);
                        } else {
                            onComplete();
                        }
                        break;
                    /*case "quml":
                        QumlWorkflow qumlWorkflow = new QumlWorkflow(callback);
                        QumlProperties qumlProperties = new QumlProperties();
                        qumlProperties.setQumlPassingPercentage(state.getStateData().getSuccessCriteria());
                        qumlProperties.setGrade((Integer) runtimeProperties.get("grade"));
                        qumlProperties.setSubject((String) runtimeProperties.get("subject"));
                        qumlProperties.setStudentName((String) runtimeProperties.get(WorkflowProperty.STUDENT));
                        qumlProperties.setStateGrade(state.getGradeNumber());
                        refIdList = WorkflowUtils.getRefId(subject, state.getGradeNumber(), workflowConfig.getChapterMapping());
                        qumlProperties.setQuestionSetId(refIdList);
                        qumlProperties.setHasuraToken((String) runtimeProperties.get(WorkflowProperty.HASURA_TOKEN));
                        Log.i("WorkFlow", "Quml Module Started with " + qumlProperties);
                        qumlWorkflow.setProps(qumlProperties);
                        qumlWorkflow.onStart(context);
                        break;*/
                    case ODK:
                        OdkWorkflow odkWorkflow1 = new OdkWorkflow(callback);
                        OdkProperties odkProperties = new OdkProperties();
                        odkProperties.setGrade((Integer) runtimeProperties.get(WorkflowProperty.GRADE));
                        odkProperties.setSubject((String) runtimeProperties.get(WorkflowProperty.SUBJECT));
                        odkProperties.setStudentCount((Integer) runtimeProperties.get(WorkflowProperty.CURRENT_STUDENT_COUNT));
                        odkProperties.setSchoolData((SchoolsData) runtimeProperties.get(WorkflowProperty.SCHOOL_DATA));
                        odkProperties.setCompetencyName((String) runtimeProperties.get(WorkflowProperty.SELECTED_COMPETENCY));
                        String odkCompetencyID = (String) runtimeProperties.get(WorkflowProperty.COMPETENCY_ID);
                        odkProperties.setCompetencyId(odkCompetencyID);
                        ArrayList<ChapterMapping> chapterMappingList1 = (ArrayList<ChapterMapping>) runtimeProperties.get(WorkflowProperty.CHAPTER_MAPPING_LIST);
                        RealmList<String> odkRefIdList = WorkflowUtils.getRefIdList(subject, (Integer) runtimeProperties.get("grade"),
                                chapterMappingList1, odkCompetencyID, type, prefs);
                        Timber.e("ODK flow to be opened with RefId and selected competency ID : " + odkRefIdList + " " + odkCompetencyID);
                        if (odkRefIdList == null) {
                            onComplete();
                            break;
                        }
                        odkProperties.setFormID(UtilityFunctions.selectRandomId(odkRefIdList));
                        odkWorkflow1.setProps(odkProperties);
                        if (context != null) {
                            odkWorkflow1.onStart(context);
                        } else {
                            onComplete();
                        }
                        break;
                }
            }

            @Override
            public void onComplete() {
//                streamTv.setText("Workflow completed");
                Timber.i("Completed");
//                Toast.makeText(context, "Workflow completed", Toast.LENGTH_LONG).show();
                if (type.equals(ODK)) {
//                    ((CompetencySelectionActivity) context).onWorkflowComplete();
                }
                listener.onComplete(stateResults);
                stateResults.clear();
            }

            @Override
            public void onError(String s) {
//                streamTv.setText("Workflow completed");
                Timber.i("Error Reported : " + s);
                listener.onComplete(stateResults);
                stateResults.clear();
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
                listener.onError(s);
            }
        });
    }

    public void addProperty(String key, Object value) {
        runtimeProperties.put(key, value);
    }

    public Object getProperty(String key) {
        return runtimeProperties.get(key);
    }

    private void act(List<Integer> actions) {
        for (Integer actionId : actions) {
            Action action = actionMap.get(actionId);
            processAction(action);
        }
        resume();
    }

    private void processAction(Action action) {
        Timber.i("New Action Detected : " + action.getType().name());
        switch (action.getType()) {
            case CLEAR:
                states.clear();
                break;
            case RETRY:
                break;
            case ADD_CURRENT:
                for (int i = 0; i < action.getCount(); i++) {
                    states.add(0, stateMap.get((long) action.getFutureStateId()));
                }
                break;
            case POP:
                states.removeFirst();
                break;
            case PUSH:
                for (int i = 0; i < action.getCount(); i++) {
                    states.add(stateMap.get((long) action.getFutureStateId()));
                }
                break;
        }
    }

    private void start(int gradeNumber, String subject, WorkflowListener listener) {
        Timber.i("Workflow started - Grade : " + gradeNumber + " , Subject : " + subject);
        runtimeProperties.put(WorkflowProperty.GRADE, gradeNumber);
        runtimeProperties.put(WorkflowProperty.SUBJECT, subject);
        String competencyId = (String) runtimeProperties.get(WorkflowProperty.COMPETENCY_ID);
        setWorkflowListener(listener);
        states.clear();
        for (FlowConfig flowConfigs : workflowConfig.getFlowConfigs()) {
            if (flowConfigs.getGradeNumber() == gradeNumber && flowConfigs.getSubject().equalsIgnoreCase(subject)
                    && flowConfigs.getCompetencyId().equalsIgnoreCase(competencyId)) {
                for (Integer stateId : flowConfigs.getStates()) {
                    states.add(stateMap.get(stateId.longValue()));
                    originalStates.add(stateMap.get(stateId.longValue()));
                }
                resume();
                return;
            }
        }
    }

    private void resume() {
        if (states.isEmpty()) {
            clean();
            currentStateCount = 0;
            originalStates.clear();
            listener.onComplete();
            return;
        }
        State state = states.peek();
        if (state != null && currentState != null && state.getId() == currentState.getId() && trialIndex == state.getMaxFailureAllowed()) {
            listener.onError("Max out");
            currentStateCount = 0;
            clean();
            return;
        }
        currentState = state;
        currentStateCount++;
        trialIndex++;
        listener.processState(states, state, new WorkflowModuleCallback() {
            @Override
            public void onSuccess(StateResult stateResult) {
                Timber.i(state.getType() + " : State Success");
                if (stateResult instanceof AssessmentStateResult) {
//                    ((AssessmentStateResult) stateResult).setSection((String) runtimeProperties.get(WorkflowProperty.GRADE_SECTION));
//                    ((AssessmentStateResult) stateResult).setStudentId((Integer) runtimeProperties.get(WorkflowProperty.STUDENT_ID));
                    ((AssessmentStateResult) stateResult).setStudentName("Student " + runtimeProperties.get(WorkflowProperty.CURRENT_STUDENT_COUNT));
                    ((AssessmentStateResult) stateResult).setCurrentStudentCount((Integer) runtimeProperties.get(WorkflowProperty.CURRENT_STUDENT_COUNT));
                    ((AssessmentStateResult) stateResult).setSchoolsData((SchoolsData) runtimeProperties.get(WorkflowProperty.SCHOOL_DATA));
                    ((AssessmentStateResult) stateResult).setCompetency((String) runtimeProperties.get(WorkflowProperty.SELECTED_COMPETENCY));
                    ((AssessmentStateResult) stateResult).setCompetencyId((String) runtimeProperties.get(WorkflowProperty.COMPETENCY_ID));
//                    Log.e("-->>a", "student name" + ((AssessmentStateResult) stateResult).getStudentName());
//                    Log.e("-->>a", "schools data" + ((AssessmentStateResult) stateResult).getSchoolsData().getSchoolName());
//                    Log.e("-->>a", "competency" + ((AssessmentStateResult) stateResult).getCompetency());
                }
                stateResults.add(stateResult);
//                Log.e("-->>", "add sate data after result screen : " + stateResult);
                clean();
                State currentState = states.peek();
                if (currentState != null) {
                    act(currentState.getDecision().getSuccessActions());
                } else {
//                    Log.e("-->>", "currentstate is null");
                }
            }

            @Override
            public void onFailure(StateResult stateResult) {
                Timber.i(state.getType() + " : State Failure");
                if (stateResult instanceof AssessmentStateResult) {
//                    ((AssessmentStateResult) stateResult).setSection((String) runtimeProperties.get(WorkflowProperty.GRADE_SECTION));
//                    ((AssessmentStateResult) stateResult).setStudentId((Integer) runtimeProperties.get(WorkflowProperty.STUDENT_ID));
                    ((AssessmentStateResult) stateResult).setStudentName((String) "Student " + runtimeProperties.get(WorkflowProperty.CURRENT_STUDENT_COUNT));
                    ((AssessmentStateResult) stateResult).setSchoolsData((SchoolsData) runtimeProperties.get(WorkflowProperty.SCHOOL_DATA));
                    ((AssessmentStateResult) stateResult).setCurrentStudentCount((Integer) runtimeProperties.get(WorkflowProperty.CURRENT_STUDENT_COUNT));
                    ((AssessmentStateResult) stateResult).setCompetency((String) runtimeProperties.get(WorkflowProperty.SELECTED_COMPETENCY));
                    ((AssessmentStateResult) stateResult).setCompetencyId((String) runtimeProperties.get(WorkflowProperty.COMPETENCY_ID));
                }
                stateResults.add(stateResult);
                State currentState = states.peek();
                if (currentState != null) {
                    act(currentState.getDecision().getFailureActions());
                }else{
                    listener.onComplete();
                }
            }
        });
    }

    public List<StateResult> getStateResults() {
        return stateResults;
    }

    private void clean() {
        trialIndex = 0;
        currentState = null;

    }

    public interface WorkflowListener {

        void processState(LinkedList<State> states, State state, WorkflowModuleCallback callback);

        void onComplete();

        void onError(String s);

    }

    public interface WorkflowResultListener {

        void onComplete(List<StateResult> stateResults);

        void onError(String s);

    }

    public void setWorkflowListener(WorkflowListener listener) {
        this.listener = listener;
    }

    public LinkedList<State> getOriginalStates() {
        return originalStates;
    }

    public Integer getCurrentStateCount() {
        return currentStateCount;
    }

    public Map<String, String> getDisplayValues() {
        return displayValuesMapper;
    }

    public void setDisplayValues(Map<String, String> displayValuesMap) {
        this.displayValuesMapper = displayValuesMap;
    }
}

