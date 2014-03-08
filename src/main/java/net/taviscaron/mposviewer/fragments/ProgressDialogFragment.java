package net.taviscaron.mposviewer.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import net.taviscaron.mposviewer.R;

/**
 * Progress dialog fragment
 * @author Andrei Senchuk
 */
public class ProgressDialogFragment extends DialogFragment {
    public static final String FRAGMENT_TAG = "ProgressDialogFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle(R.string.message_loading_please_wait_title);
        progressDialog.setMessage(getActivity().getString(R.string.message_loading_please_wait_body));
        return progressDialog;
    }
}
