package com.example.idamusic_mobile;

import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.idamusic_mobile.DummyContent.DummyItem;
import com.example.idamusic_mobile.ItemFragment.OnListFragmentInteractionListener;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SongSelectActivityAdapter extends RecyclerView.Adapter<SongSelectActivityAdapter.ViewHolder> {

    private final Songs mSongs;
    private final selectSongListener mListener;

    public SongSelectActivityAdapter(Songs songs, selectSongListener listener) {
        mSongs = songs;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item_songs, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mSong = mSongs.mSongs.get(position);
        holder.mNameSong.setText(holder.mSong.getTitle());
        holder.mIdSong.setText(position + 1 +"");
        holder.mArtistSong.setText(holder.mSong.getArtist());
        //holder.mIdView.setText(mValues.get(position).playable.artist);
        //holder.mContentView.setText(mValues.get(position).playable.name);
        // BitmapDrawable ob = new BitmapDrawable(mValues.get(position).playable.image);
        //    holder.mWebView.setImageDrawable(ob);
        // holder.mView.setBackground(ob);
        holder.mImageViewPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("SongSelect", "OnClick");
                v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onSongSelect(holder.mSong.getUri());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSongs.mSongs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdSong;
        public final TextView mNameSong;
        public final TextView mArtistSong;
        public final ImageView mImageViewPlay;
        public Song mSong;
;

        //        public final ImageView mWebVie
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdSong = (TextView) view.findViewById(R.id.textSongNumber);
            mNameSong= (TextView) view.findViewById(R.id.textSongTitle);
            mImageViewPlay = (ImageView) view.findViewById(R.id.imageViewSongPlay);
            mArtistSong = (TextView) view.findViewById(R.id.textSongArtist);
//            mWebView = (ImageView) view.findViewById(R.id.imageView3);


        }

        @Override
        public String toString() {
            return super.toString() + " '" + mNameSong.getText() + "'";
        }
    }

    public interface selectSongListener{
        public void onSongSelect(String uri);
    }
}
