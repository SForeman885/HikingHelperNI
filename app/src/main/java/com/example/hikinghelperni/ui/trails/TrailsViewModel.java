package com.example.hikinghelperni.ui.trails;

import androidx.lifecycle.ViewModel;

import com.example.hikinghelperni.TrailDetailsDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrailsViewModel extends ViewModel {

    private String mTrailId;
    private TrailDetailsDTO mSelectedTrailDetails;

    public TrailsViewModel() {
        mTrailId = "";
    }

}