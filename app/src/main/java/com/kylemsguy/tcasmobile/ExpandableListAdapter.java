package com.kylemsguy.tcasmobile;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.kylemsguy.tcasmobile.backend.QAObject;
import com.kylemsguy.tcasmobile.backend.Question;

import java.util.Collections;
import java.util.List;


public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private static final boolean REVERSE_ENTRIES = true;

    private Context mContext;

    private List<Question> mQuestions;

    public ExpandableListAdapter(Context context, List<Question> questions) {
        mContext = context;
        mQuestions = questions;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mQuestions.get(groupPosition).getAnswers().get(childPosition).getContent();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
        TextView listItemName = (TextView) convertView.findViewById(R.id.list_item_name);

        listItemName.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mQuestions.get(groupPosition).getAnswers().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mQuestions.get(groupPosition).getContent();
    }

    @Override
    public int getGroupCount() {
        return mQuestions.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.list_group, null);
        }

        TextView listHeaderName = (TextView) convertView.findViewById(R.id.list_header_name);
        listHeaderName.setTypeface(null, Typeface.BOLD);
        listHeaderName.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void reloadItems(List<Question> questions) {
        mQuestions.clear();
        if(REVERSE_ENTRIES) {
            for(int i = questions.size(); i > 0; i--){
                mQuestions.add(questions.get(i-1).getReversed());
            }
        }
        else{
            mQuestions.addAll(questions);
        }
    }
}
