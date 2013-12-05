package com.yulinghb.watermonitor;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class FragmentDeletePrompt extends DialogFragment {
	
	public interface OnDeleteListener{
		void comfirm();
		void cancel(); 
		int getMessageId();
	}

	OnDeleteListener mCallback;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(mCallback.getMessageId())
				.setPositiveButton(R.string.txt_yes, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mCallback.comfirm();
					}
				})
				.setNegativeButton(R.string.txt_no, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mCallback.cancel();
					}
				});
				
		return builder.create();
	}
	
	public void setCallback(OnDeleteListener listener){
		mCallback = listener;
	}

}
