package com.example.admin.wrms_weatherstation;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.speech.tts.TextToSpeech;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DateAdapter  extends RecyclerView.Adapter<DateAdapter.ViewHolder> implements Filterable,ItemTouchHelperAdapter {

    private ArrayList<String> mDataset = new ArrayList<String>();
    private ArrayList<String> imeiList = new ArrayList<String>();
    private  ArrayList<String> listSuggesion = new ArrayList<String>();
    private  ArrayList<String> totalRFList = new ArrayList<String>();
    public Context mContext;
    String imageString;
    TextToSpeech t1;

    DBAdapter db;
    private ItemFilter mFilter = new ItemFilter();

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {

        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(imeiList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(imeiList, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);

    }

    @Override
    public void onItemDismiss(int position) {
        imeiList.remove(position);
        notifyItemRemoved(position);
    }

    // Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView date,imeiTV,totalTV;

        public RelativeLayout detailRow;



        public ViewHolder(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.date_row);
            imeiTV = (TextView)v.findViewById(R.id.imei_row);
            totalTV = (TextView)v.findViewById(R.id.total_row);
            detailRow = (RelativeLayout)v.findViewById(R.id.detail_row);
            db = new DBAdapter(mContext);
            db.open();
        }
    }

      /*public void add(int position, String item) {
          mDataset.add(position, item);
          notifyItemInserted(position);
      }*/

    public void remove(int pos) {
        //   int position = mDataset.indexOf(item);
        imeiList.remove(pos);
        notifyItemRemoved(pos);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DateAdapter(Context con, ArrayList<String> myDataset,ArrayList<String> imeiL,ArrayList<String> trfList) {
        mDataset = myDataset;
        imeiList = imeiL;
        listSuggesion = imeiL;
        totalRFList = trfList;
        mContext = con;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public DateAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_row, parent, false);

        DateAdapter.ViewHolder vh = new DateAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final DateAdapter.ViewHolder holder, final int position) {

        // holder.setIsRecyclable(false);
        holder.date.setText(mDataset.get(position));
        holder.imeiTV.setText(imeiList.get(position));
        if (totalRFList.size()>position){
            holder.totalTV.setText(totalRFList.get(position));
        }

        holder.detailRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db = new DBAdapter(mContext);
                db.open();
                String strDate = mDataset.get(position);
                String imei_selected = imeiList.get(position);

                ArrayList<WeatherSample> list = new ArrayList<WeatherSample>();

                if (strDate!=null && strDate.length()>4) {
                    Cursor dateByCursor = db.getDataByDate(strDate,imei_selected);
                   // Cursor dateByCursor = db.getAllData();
                    Log.v("dateByCursor_count", "," + dateByCursor.getCount());
                    if (dateByCursor.moveToFirst()) {
//
                        do {
                            WeatherSample bean = new WeatherSample();
                            String dattaa = dateByCursor.getString(dateByCursor.getColumnIndex(DBAdapter.DATE));
                            String rainfallll = dateByCursor.getString(dateByCursor.getColumnIndex(DBAdapter.RAINFALL));
                            String timeee = dateByCursor.getString(dateByCursor.getColumnIndex(DBAdapter.HOURS));
                            bean.setDate(dattaa);
                            bean.setSumHours(timeee);
                            bean.setRainfall(rainfallll);
                            list.add(bean);
                        }
                        while (dateByCursor.moveToNext());
                    }
                    if (list.size()>0) {
                        datePopupMethod(strDate,imei_selected,list);
                    }else {
                        Toast.makeText(mContext,"No Rainfall found for selected date",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return imeiList.size();
    }


    public void datePopupMethod(String selectedDate,String selectedIMEI,ArrayList<WeatherSample> listRainfall) {

        final Dialog dialog = new Dialog(mContext, R.style.DialogSlideAnim);

        //  final Dialog dialog = new Dialog(getActivity());

        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.BOTTOM;
        wlp.dimAmount = 0.5f;

        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
        // Include dialog.xml file
        dialog.setContentView(R.layout.rainfall_popup);


        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 1250);
        /*} else {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }*/


        RecyclerView recyclerView = (RecyclerView) dialog.findViewById(R.id.recyclerview_rainfall);
        TextView datetText = (TextView) dialog.findViewById(R.id.date_heading);
        datetText.setText(selectedDate+" ( "+selectedIMEI+" ) ");

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager ddLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(ddLayoutManager);
        // ddRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration horizontalDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        Drawable horizontalDivider = ContextCompat.getDrawable(mContext, R.drawable.horizontal_divider);
        horizontalDecoration.setDrawable(horizontalDivider);
        recyclerView.addItemDecoration(horizontalDecoration);



        if (listRainfall.size()>0){
            AdapterHistoricalRainfall adapter = new AdapterHistoricalRainfall(mContext, listRainfall);
            recyclerView.setAdapter(adapter);
        }

        dialog.show();
    }

    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            String filterString = constraint.toString().toLowerCase();


            Log.v("filterStringg",""+filterString);

            FilterResults results = new FilterResults();

            final List<String> list = listSuggesion;
            int count = list.size();
            final ArrayList<String> nlist = new ArrayList<String>(count);

            String filterableString ;

            for (int i = 0; i < count; i++) {
                filterableString = ""+list.get(i);
                if (filterableString.toLowerCase().contains(filterString)) {
                    String mBookServiceModel = list.get(i);
                    nlist.add(mBookServiceModel);
                }
            }

            results.values = nlist;
            results.count = nlist.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            imeiList = (ArrayList<String>) results.values;

            Log.v("imeiListCount",imeiList.size()+"");
            notifyDataSetChanged();
        }
    }


}