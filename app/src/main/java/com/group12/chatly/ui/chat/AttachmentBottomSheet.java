package com.group12.chatly.ui.chat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.group12.chatly.R;

/** Bảng "show. tell. send." mở khi bấm nút kẹp ghim trong màn chat. */
public class AttachmentBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AttachmentBottomSheet";

    public enum Option { GALLERY, CAMERA, FILE, LOCATION, VOICE }

    /** Activity chứa bảng này implement interface để nhận lựa chọn. */
    public interface Listener {
        void onAttachmentOption(Option option);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sheet_attachment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bind(view, R.id.opt_gallery, Option.GALLERY);
        bind(view, R.id.opt_camera, Option.CAMERA);
        bind(view, R.id.opt_file, Option.FILE);
        bind(view, R.id.opt_location, Option.LOCATION);
        bind(view, R.id.opt_voice, Option.VOICE);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Cho khung nền mặc định của bảng trong suốt để thấy nền trắng bo góc của layout
        View parent = (View) requireView().getParent();
        parent.setBackgroundColor(Color.TRANSPARENT);
    }

    private void bind(View root, @IdRes int id, Option option) {
        root.findViewById(id).setOnClickListener(v -> {
            if (getActivity() instanceof Listener) {
                ((Listener) getActivity()).onAttachmentOption(option);
            }
            dismiss();
        });
    }
}