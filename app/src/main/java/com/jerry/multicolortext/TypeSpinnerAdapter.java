package com.jerry.multicolortext;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.Serializable;

/**
 * 类型选择框适配器
 *
 * @author xujierui
 * @date 2018/9/19
 */

public class TypeSpinnerAdapter extends BaseAdapter {
    private Context context;
    private TypeBean[] typeBeanArray;

    TypeSpinnerAdapter(Context context, TypeBean[] typeBeanArray) {
        this.context = context;
        this.typeBeanArray = typeBeanArray;
    }

    @Override
    public int getCount() {
        return typeBeanArray.length;
    }

    @Override
    public Object getItem(int position) {
        if (position >= 0 && position < typeBeanArray.length) {
            return typeBeanArray[position];
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
            convertView = LayoutInflater.from(context).inflate(R.layout.item_spinner_simple, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }

        viewHolder = (ViewHolder) convertView.getTag();
        if (position >= 0 && position < typeBeanArray.length) {
            viewHolder.getTvName().setText(typeBeanArray[position].getTypeName());
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

    public static class TypeBean implements Serializable {
        private String typeName;
        private int typeId;

        public TypeBean(String typeName, int typeId) {
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
            return "TypeBean{" +
                    "typeName='" + typeName + '\'' +
                    ", typeId=" + typeId +
                    '}';
        }
    }
}
