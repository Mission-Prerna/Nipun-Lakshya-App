package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect1;
import org.odk.collect.android.provider.InstanceProvider;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.views.ProgressBar;

import java.util.Date;

import io.reactivex.disposables.CompositeDisposable;

import static org.odk.collect.android.provider.InstanceProviderAPI.STATUS_SUBMISSION_FAILED;
import static org.odk.collect.android.provider.InstanceProviderAPI.STATUS_SUBMITTED;

public class InstanceUploaderAdapter extends CursorAdapter {
    private final CompositeDisposable compositeDisposable;

    public InstanceUploaderAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        Collect1.getInstance().getComponent().inject(this);
        compositeDisposable = new CompositeDisposable();
    }

    public void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.form_chooser_list_item_multiple_choice, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        long lastStatusChangeDate = getCursor().getLong(getCursor().getColumnIndex(InstanceColumns.LAST_STATUS_CHANGE_DATE));
        String status = cursor.getString(cursor.getColumnIndex(InstanceColumns.STATUS));

        viewHolder.formTitle.setText(cursor.getString(cursor.getColumnIndex(InstanceColumns.DISPLAY_NAME)));
        viewHolder.formSubtitle.setText(InstanceProvider.getDisplaySubtext(context, status, new Date(lastStatusChangeDate)));

        switch (status) {
            case STATUS_SUBMISSION_FAILED:
                viewHolder.statusIcon.setImageResource(R.drawable.form_state_submission_failed_circle);
                break;

            case STATUS_SUBMITTED:
                viewHolder.statusIcon.setImageResource(R.drawable.form_state_submitted_circle);
                break;

            default:
                viewHolder.statusIcon.setImageResource(R.drawable.form_state_finalized_circle);
        }
    }

    static class ViewHolder {
        TextView formTitle;
        TextView formSubtitle;
        CheckBox checkbox;
        ProgressBar progressBar;
        ImageView statusIcon;
        ImageView closeButton;

        ViewHolder(View view) {
            formTitle = view.findViewById(R.id.form_title);
           formSubtitle = view.findViewById(R.id.form_subtitle);
            checkbox = view.findViewById(R.id.checkbox);
            progressBar = view.findViewById(R.id.progress_bar);
            statusIcon = view.findViewById(R.id.image);
            closeButton = view.findViewById(R.id.close_box);
        }
    }
}
