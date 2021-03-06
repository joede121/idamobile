package com.example.idamusic_mobile;

import android.graphics.DiscretePathEffect;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.idamusic_mobile.ItemFragment.OnListFragmentInteractionListener;
import com.example.idamusic_mobile.DummyContent.DummyItem;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    int mFactor = 2;
    Display mDisplay;

    public MyItemRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener, Display display) {
        mValues = items;
        mListener = listener;
        mDisplay = display;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_2x2, parent, false);
        return new ViewHolder(view);
    }

    public void changeLayout(int factor){
        mFactor = factor;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mImageViewBg.getLayoutParams().width = mDisplay.getWidth() / mFactor;
        holder.mImageViewBg.getLayoutParams().height = mDisplay.getWidth() / mFactor;
        holder.mImageViewPlay.getLayoutParams().height = mDisplay.getWidth() / ( mFactor * 3 );
        holder.mImageViewPlay.getLayoutParams().width = mDisplay.getWidth() / ( mFactor * 3 );
        holder.mItem = mValues.get(position);
        //holder.mIdView.setText(mValues.get(position).playable.artist);
        //holder.mContentView.setText(mValues.get(position).playable.name);
        // BitmapDrawable ob = new BitmapDrawable(mValues.get(position).playable.image);
        //    holder.mWebView.setImageDrawable(ob);
        // holder.mView.setBackground(ob);
        BitmapDrawable ob = new BitmapDrawable(mValues.get(position).playable.image);
        holder.mImageViewBg.setImageDrawable(ob);
        holder.mImageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ImageView mImageViewPlay;
        public DummyItem mItem;
        public final ImageView mImageViewBg;

        //        public final ImageView mWebVie
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
            mImageViewPlay = (ImageView) view.findViewById(R.id.imageViewPlay);
            mImageViewBg = (ImageView) view.findViewById(R.id.imageViewBg);
//            mWebView = (ImageView) view.findViewById(R.id.imageView3);


        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
