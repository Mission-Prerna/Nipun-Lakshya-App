package com.samagra.ancillaryscreens.custom.tagview;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.samagra.ancillaryscreens.AncillaryScreensDriver;
import com.samagra.ancillaryscreens.R;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Utils {

	private Utils() throws InstantiationException {
		throw new InstantiationException("This class is not for instantiation");
	}

	public static int dipToPx(Context c,float dipValue) {
		DisplayMetrics metrics = c.getResources().getDisplayMetrics();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
	}

	public static Tag getTagWithDefaultConfig(Context context, String text){
		Tag tag = new Tag(text);
		tag.setRadius(30f);
		tag.setLayoutColor(ContextCompat.getColor(context, R.color.color_primary));
		tag.setTagTextColor(ContextCompat.getColor(context, R.color.white));
		tag.setLayoutBorderColor(ContextCompat.getColor(context, R.color.color_primary));
		tag.setLayoutBorderSize(1.5f);
		tag.setTagTextSize(14f);
		tag.setDeletable(true);
		return tag;
	}

	/*public static List<Subject> getSuperSubjects(){
		String eligibleGradeSubject = AncillaryScreensDriver.mFirebaseRemoteConfig.getString("eligible_grade_subject");
		Type listType = new TypeToken<List<Subject>>() {}.getType();
		return new Gson().fromJson(eligibleGradeSubject, listType);
		*//*ArrayList<Subject> superList = new ArrayList<>();
		superList.add(new Subject("Vocational Subjects", 11));
		superList.add(new Subject("Social Science", 10));
		superList.add(new Subject("Science", 10));
		superList.add(new Subject("Maths", 10));
		superList.add(new Subject("English", 10));
		superList.add(new Subject("Hindi", 10));
		superList.add(new Subject("Philosophy", 11));
		superList.add(new Subject("English", 11));
		superList.add(new Subject("Maths", 11));
		superList.add(new Subject("Political Science", 11));
		superList.add(new Subject("Geography", 11));
		superList.add(new Subject("Accountancy", 11));
		superList.add(new Subject("Business Studies", 11));
		superList.add(new Subject("Economics", 11));
		superList.add(new Subject("Physics", 11));
		superList.add(new Subject("Hindi", 11));
		superList.add(new Subject("Chemistry", 11));
		superList.add(new Subject("Biology", 11));
		superList.add(new Subject("History", 11));
		superList.add(new Subject("Physical Education", 11));
		superList.add(new Subject("Music", 11));
		superList.add(new Subject("Psychology", 11));
		superList.add(new Subject("Public Admn.", 11));
		superList.add(new Subject("Sanskrit.", 11));
		superList.add(new Subject("Home Science", 11));
		superList.add(new Subject("Sociology.", 11));
		superList.add(new Subject("English.", 12));
		superList.add(new Subject("Physics.", 12));
		superList.add(new Subject("Chemistry.", 12));
		superList.add(new Subject("Maths.", 12));
		superList.add(new Subject("Biology.", 12));
		superList.add(new Subject("Physical Education.", 12));
		superList.add(new Subject("Music.", 12));
		superList.add(new Subject("Sociology.", 12));
		superList.add(new Subject("Home Science.", 12));
		superList.add(new Subject("Sanskrit", 12));
		superList.add(new Subject("History", 12));
		superList.add(new Subject("Political Science", 12));
		superList.add(new Subject("Hindi", 12));
		superList.add(new Subject("Public Admn.", 12));
		superList.add(new Subject("Geography", 12));
		superList.add(new Subject("Psychology", 12));
		superList.add(new Subject("Philosophy", 12));
		superList.add(new Subject("Vocational Subjects", 12));
		superList.add(new Subject("Business Studies", 12));
		superList.add(new Subject("Accountancy", 12));
		superList.add(new Subject("Economics", 12));
		superList.add(new Subject("Computer Science", 11));
		superList.add(new Subject("Computer Science", 12));
		return superList;*//*
	}*/

	/*@NotNull
	public static ArrayList<String> getListOfDesignations() {
		String designations = AncillaryScreensDriver.mFirebaseRemoteConfig.getString("designations");
		Type listType = new TypeToken<List<String>>() {}.getType();
		ArrayList<String> designationList = new Gson().fromJson(designations, listType);
		return designationList;
	}

	@NotNull
	public static ArrayList<String> getListOfSubjects() {
		String subjects = AncillaryScreensDriver.mFirebaseRemoteConfig.getString("subjects");
		Type listType = new TypeToken<List<String>>() {}.getType();
		ArrayList<String> subjectsList = new Gson().fromJson(subjects, listType);
		return subjectsList;
	}*/
}