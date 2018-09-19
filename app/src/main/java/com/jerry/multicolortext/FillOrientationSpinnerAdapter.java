package com.jerry.multicolortext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.Serializable;

/**
 * 填充方向选择框的适配器
 *
 * @author xujierui
 * @date 2018/9/19
 */

public class FillOrientationSpinnerAdapter extends BaseAdapter {
    private Context context;
    private FillOrientationType[] fillOrientationTypeArray;

    FillOrientationSpinnerAdapter(Context context, FillOrientationType[] fillOrientationTypeArray) {
        this.context = context;
        this.fillOrientationTypeArray = fillOrientationTypeArray;
    }

    @Override
    public int getCount() {
        return fillOrientationTypeArray.length;
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < fillOrientationTypeArray.length) {
            return fillOrientationTypeArray[position];
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_fill_orientation, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position >= 0 && position < fillOrientationTypeArray.length) {
            viewHolder.getTvName().setText(fillOrientationTypeArray[position].getTypeName());
        }
        return convertView;
    }

    private class ViewHolder {
        private TextView tvName;

        ViewHolder(View rootView) {
            this.tvName = (TextView) rootView.findViewById(R.id.tv_type_name);
        }

        TextView getTvName() {
            return tvName;
        }

        public void setTvName(TextView tvName) {
            this.tvName = tvName;
        }
    }

    public static class FillOrientationType implements Serializable {
        private String typeName;
        private int typeId;

        public FillOrientationType(String typeName, int typeId) {

            this.typeName = typeName;
            this.typeId = typeId;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public int getTypeId() {
            return typeId;
        }

        public void setTypeId(int typeId) {
            this.typeId = typeId;
        }

        @Override
        public String toString() {
            return "FillOrientationType{" +
                    "typeName='" + typeName + '\'' +
                    ", typeId=" + typeId +
                    '}';
        }
    }
}
