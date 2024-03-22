package com.samagra.parent.ui.formlite.form;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.samagra.parent.R;
import com.samagra.parent.databinding.FragmentFormBinding;
import com.samagra.parent.ui.formlite.AppUtility;
import com.samagra.parent.ui.formlite.FormConstants;
import com.samagra.parent.ui.formlite.RegistrationManager;
import com.samagra.parent.ui.formlite.model.DropdownOption;
import com.samagra.parent.ui.formlite.model.FieldSelections;
import com.samagra.parent.ui.formlite.model.InputField;
import com.samagra.parent.ui.formlite.model.Localiation;
import com.samagra.parent.ui.formlite.widgets.AppSpinner;
import com.samagra.parent.ui.formlite.widgets.AppSpinnerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentDetailFormFragment extends Fragment {
    private FragmentFormBinding mBinding;
    private List<InputField> formFields;
    private Localiation dictionary;
    private FormLiteViewModel vm;
    private SubmissionListener submissionListener;

    public StudentDetailFormFragment(SubmissionListener submissionListener) {
        this.submissionListener = submissionListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.fragment_form,
                container, false);
        vm = new ViewModelProvider(this).get(FormLiteViewModel.class);
        parseData();
        return mBinding.getRoot();
    }

    private void parseData() {

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registerListeners();
        mBinding.btnAction.setOnClickListener(view1 -> {
            Map<String, Object> formMapper = validateForm();
            if (formMapper != null) {
                formMapper.put("submittedAt", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(new Date()));
//                    vm.registerStudent(reqObj);
            }
            if (submissionListener != null) {
                if (formMapper == null) {
                    submissionListener.onError();
                    return;
                }
                submissionListener.onFormSubmitted(formMapper);
            }
        });
        updateUI();
    }

    private Map<String, Object> validateForm() {
        boolean isValid = true;
        for (InputField inputField : formFields) {
            if (inputField.getRequired() && (inputField.getValue() == null || TextUtils.isEmpty(inputField.getValue().toString()))) {
                isValid = false;
                if (inputField.getWidget().equalsIgnoreCase(FormConstants.WidgetType.INPUT) ||
                        inputField.getWidget().equalsIgnoreCase(FormConstants.WidgetType.VERIFICATION)) {
                    if (TextUtils.isEmpty(inputField.getValidationMessage())) {
                        inputField.setErrorMessage(String.format(getString(R.string.please_enter),
                                dictionary.getHi().get(inputField.getLabel())));
                    } else {
                        inputField.setErrorMessage(dictionary.getHi().get(inputField.getValidationMessage()));
                    }
                } else {
                    if (TextUtils.isEmpty(inputField.getValidationMessage())) {
                        inputField.setErrorMessage(String.format(getString(R.string.please_select),
                                dictionary.getHi().get(inputField.getLabel())));
                    } else {
                        inputField.setErrorMessage(dictionary.getHi().get(inputField.getValidationMessage()));
                    }
                }
            } else {
                if (!inputField.getRequired() && !inputField.isVisible()) {
                    inputField.setVisible(null);
                }
                if (AppUtility.isValueExist(inputField.getValue()) && !TextUtils.isEmpty(inputField.getValidation())) {
                    Pattern p = Pattern.compile(inputField.getValidation());
                    Matcher m = p.matcher(inputField.getValue().toString());
                    boolean isMatched = m.matches();
                    if (!isMatched) {
                        isValid = false;
                        inputField.setErrorMessage(String.format(getString(R.string.please_enter_valid),
                                dictionary.getHi().get(inputField.getLabel())));
                        continue;
                    }
                }
                inputField.setErrorMessage(null);
                vm.getDataStore().put(inputField.getKey(), inputField.getValue());
            }
        }
        if (isValid) {
            return vm.getDataStore();
        } else {
            updateUI();
            return null;
        }
    }

    private void updateUI() {
        if (formFields == null) {
            vm.setFormData(AppUtility.getStudentDetailsFormFields());
            formFields = vm.getFormData().getFormFields();
            dictionary = RegistrationManager.getInstance().getGlobalDictionary();
            boolean disabilityTypeVisibile = false;
            InputField disabilityField = null;
        }

        if (formFields != null) {
            mBinding.llyFormFieldContainer.removeAllViews();
            for (int i = 0; i < formFields.size(); i++) {
                InputField inputField = formFields.get(i);
                inflateField(inputField, i);
            }
        }
    }

    private void inflateField(InputField inputField, int position) {
        if (inputField.isVisible()) {
            switch (inputField.getWidget()) {
                case FormConstants.WidgetType.DROPDOWN:
                    mBinding.llyFormFieldContainer.addView(getDropdown(inputField));
                    break;

              /*  case FormConstants.WidgetType.INPUT:
                    mBinding.llyFormFieldContainer.addView(getInputView(inputField));
                    break;

                case FormConstants.WidgetType.RADIO_GROUP:
                    mBinding.llyFormFieldContainer.addView(getRadioGroup(inputField));
                    break;

                case FormConstants.WidgetType.DATE_INPUT:
                    mBinding.llyFormFieldContainer.addView(getDateInputView(inputField));
                    break;

                case FormConstants.WidgetType.VERIFICATION:
                    mBinding.llyFormFieldContainer.addView(getVerificationView(inputField));
                    break;*/
            }
        }
    }

    /*private IDVerificationView getVerificationView(InputField inputField) {
        IDVerificationView verificationView = new IDVerificationView(getActivity());
        if (inputField.getRequired() != null && inputField.getRequired()) {
            verificationView.setTitle(dictionary.getHi().get(inputField.getLabel()) + " *");
        } else {
            verificationView.setTitle(dictionary.getHi().get(inputField.getLabel()));
        }
        verificationView.setVerifyDataCallback(s -> vm.isAadhaarValid(s));
        verificationView.setData(inputField);
        verificationView.getIsIDVerified().observe(getViewLifecycleOwner(), isVerified -> {
            if (isVerified) {
                inputField.setValue(verificationView.getValue());
            } else {
                inputField.setValue(null);
            }
        });
        return verificationView;
    }
*/
    private AppSpinner getDropdown(InputField inputField) {
        AppSpinner spinner = new AppSpinner(getActivity(), inputField.getKey());
        if (inputField.getRequired() != null && inputField.getRequired()) {
            spinner.setTitle(dictionary.getHi().get(inputField.getLabel()) + " *");
        } else {
            spinner.setTitle(dictionary.getHi().get(inputField.getLabel()));
        }
        spinner.setData(inputField);
        spinner.setItemSelectionListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DropdownOption selectedItem = (DropdownOption) spinner.getSelectedItem();
                if (!selectedItem.isPlaceHolder()) {
                    inputField.setValue(selectedItem.getValue());
                    if (selectedItem.getFieldSelections() != null) {
                        boolean isUpdated = false;
                        for (int i = 0; i < selectedItem.getFieldSelections().size(); i++) {
                            FieldSelections selectionFields = selectedItem.getFieldSelections().get(i);
                            InputField fieldToDecide = AppUtility.getField(formFields, selectionFields.getKey());
                            if (fieldToDecide != null && fieldToDecide.isVisible() != selectionFields.getSelected()) {
                                fieldToDecide.setVisible((Boolean) selectionFields.getSelected());
                                fieldToDecide.setRequired((Boolean) selectionFields.getSelected());
                                isUpdated = true;
                            }
                        }
                        if (isUpdated) {
                            updateUI();
                        }
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        String placeholder = TextUtils.isEmpty(inputField.getPlaceholder())
                ? getString(R.string.select) + " " + dictionary.getHi().get(inputField.getLabel())
                : dictionary.getHi().get(inputField.getPlaceholder());
        AppSpinnerAdapter adapter = new AppSpinnerAdapter(getContext(), dictionary.getHi(), false, placeholder);
        List<DropdownOption> options = new ArrayList<>(inputField.getOptions());
        adapter.setData(options);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        String selectedValue = (AppUtility.isValueExist(inputField.getValue())) ? inputField.getValue().toString() : "";
        int selectedPosition = 0;
        for (int i = 0; i < options.size(); i++) {
            DropdownOption option = options.get(i);
            if (option.getValue().toString().equalsIgnoreCase(selectedValue)) {
                selectedPosition = i;
                break;
            }
        }
        spinner.setSelection(selectedPosition);
        return spinner;
    }

   /* public AppInputBox getInputView(InputField inputField) {
        AppInputBox inputBox = new AppInputBox(getActivity(), inputField.getKey());
        if (inputField.getRequired() != null && inputField.getRequired()) {
            inputBox.setInputboxTitle(dictionary.getHi().get(inputField.getLabel()) + " *");
        } else {
            inputBox.setInputboxTitle(dictionary.getHi().get(inputField.getLabel()));
        }
        inputBox.setData(inputField);
        if (AppUtility.isValueExist(inputField.getValue())) {
            inputBox.setInputboxText(inputField.getValue().toString());
        }
        inputBox.addInputBoxWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                inputField.setValue(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        return inputBox;
    }

    public AppRadioGroup getRadioGroup(InputField inputField) {
        AppRadioGroup radioGroup = new AppRadioGroup(getActivity(), inputField.getKey());
        if (inputField.getRequired() != null && inputField.getRequired()) {
            radioGroup.setTitle(dictionary.getHi().get(inputField.getLabel()) + " *");
        } else {
            radioGroup.setTitle(dictionary.getHi().get(inputField.getLabel()));
        }
        radioGroup.setData(inputField, dictionary.getHi(),
                AppUtility.isValueExist(inputField.getValue()) ? inputField.getValue().toString() : "");
        radioGroup.setCheckChangeListener(new OptionSelectionListener<DropdownOption>() {
            @Override
            public void onOptionChange(DropdownOption selectedItem) {
                inputField.setValue(selectedItem.getValue());
                if (inputField.getKey().equalsIgnoreCase("isCwsn")) {
                    InputField disabilityField = EnrollmentUtility.getField(formFields, "disabilityType");
                    if (disabilityField != null && disabilityField.isVisible() != (Boolean) selectedItem.getValue()) {
                        disabilityField.setVisible((Boolean) selectedItem.getValue());
                        disabilityField.setRequired((Boolean) selectedItem.getValue());
                        updateUI();
                    }

                }
            }
        });

        return radioGroup;
    }

    public DateInputBox getDateInputView(InputField inputField) {
        Date dob = null;
        if (AppUtility.isValueExist(inputField.getValue())) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                dob = sdf.parse(inputField.getValue().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DateInputBox dateInputBox = new DateInputBox(getActivity(), inputField.getKey());
        dateInputBox.setDate(dob);
        dateInputBox.setMaxDate(new Date());
        if (inputField.getRequired() != null && inputField.getRequired()) {
            dateInputBox.setInputboxTitle(dictionary.getHi().get(inputField.getLabel()) + " *");
        } else {
            dateInputBox.setInputboxTitle(dictionary.getHi().get(inputField.getLabel()));
        }
        inputField.setValue(dateInputBox.getFormattedDate("YYYY-MM-dd"));
        dateInputBox.setOnDateChangeListener(new DateInputBox.OnDateChangeListener() {
            @Override
            public void onDateChange(Date date) {
                Log.i("Hema formattedDate", dateInputBox.getFormattedDate("YYYY-MM-dd"));
                inputField.setValue(dateInputBox.getFormattedDate("YYYY-MM-dd"));
            }
        });
        return dateInputBox;
    }*/

    private void registerListeners() {

    }

}
