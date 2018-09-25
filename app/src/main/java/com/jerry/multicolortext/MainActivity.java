package com.jerry.multicolortext;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.jerry.multicolortext.TypeSpinnerAdapter.TypeBean;


import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 演示页面
 *
 * @author xujierui
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.mctv_show)
    MultiColorTextView mctvShow;
    @BindView(R.id.spinner_divider_type)
    Spinner spinnerDividerType;
    @BindView(R.id.spinner_shape_type)
    Spinner spinnerShapeType;
    @BindView(R.id.sb_fill_progress_controller)
    SeekBar sbFillProgressController;
    @BindView(R.id.tv_fill_progress_percent)
    TextView tvFillProgressPercent;

    TypeBean[] dividerTypeArray;
    TypeBean[] shapeTypeArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initData();
        initListener();
    }

    private void initData() {
        dividerTypeArray = new TypeBean[2];
        dividerTypeArray[0] = new TypeBean("直线", MultiColorTextView.DIVIDER_TYPE_LINE);
        dividerTypeArray[1] = new TypeBean("贝塞尔曲线", MultiColorTextView.DIVIDER_TYPE_BESSEL);
        TypeSpinnerAdapter orientationAdapter = new TypeSpinnerAdapter(this, dividerTypeArray);
        spinnerDividerType.setAdapter(orientationAdapter);

        shapeTypeArray = new TypeBean[3];
        shapeTypeArray[0] = new TypeBean("矩形", MultiColorTextView.SHAPE_TYPE_RECT);
        shapeTypeArray[1] = new TypeBean("圆形", MultiColorTextView.SHAPE_TYPE_CIRCLE);
        shapeTypeArray[2] = new TypeBean("圆角矩形", MultiColorTextView.SHAPE_TYPE_ROUND_RECT);
        TypeSpinnerAdapter shapeAdapter = new TypeSpinnerAdapter(this, shapeTypeArray);
        spinnerShapeType.setAdapter(shapeAdapter);

        mctvShow.setFillProgress(sbFillProgressController.getProgress() * 1.0f / sbFillProgressController.getMax());
    }

    private void initListener() {
        spinnerDividerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < dividerTypeArray.length) {
                    mctvShow.setDividerType(dividerTypeArray[position].getTypeId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mctvShow.setDividerType(MultiColorTextView.DIVIDER_TYPE_DEFAULT);
            }
        });
        spinnerShapeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < shapeTypeArray.length) {
                    mctvShow.setShapeType(shapeTypeArray[position].getTypeId());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mctvShow.setShapeType(MultiColorTextView.SHAPE_TYPE_DEFAULT);
            }
        });
        sbFillProgressController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvFillProgressPercent.setText(getApplicationContext().getString(R.string.percent, String.valueOf(Math.min(Math.round(progress * 1.0f / sbFillProgressController.getMax() * 100), 100))));
                mctvShow.setFillProgress(progress * 1.0f / sbFillProgressController.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
