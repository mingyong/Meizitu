package me.scola.picrawler.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

//import com.example.staggeredgridviewdemo.loader.ImageLoader;
//import com.example.staggeredgridviewdemo.views.ScaleImageView;

import com.android.volley.toolbox.ImageLoader;

import java.util.ArrayList;

import me.scola.picrawler.app.R;
import me.scola.picrawler.data.ImageCacheManager;
import me.scola.picrawler.util.CLog;
import me.scola.picrawler.view.ScaleImageView;

public class StaggeredAdapter extends ArrayAdapter<String> {

	private ImageLoader mLoader;
	private Drawable mDefaultImageDrawable = new ColorDrawable(Color.argb(255, 201, 201, 201));

	public StaggeredAdapter(Context context, int textViewResourceId,
			ArrayList<String> objects) {
		super(context, textViewResourceId, objects);
//		mLoader = new ImageLoader(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater layoutInflator = LayoutInflater.from(getContext());
			convertView = layoutInflator.inflate(R.layout.row_staggered_demo,
					null);
			holder = new ViewHolder();
			holder.imageView = (ScaleImageView) convertView .findViewById(R.id.imageView1);
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();

//		mLoader.DisplayImage(getItem(position), holder.imageView);
		CLog.d("img url: " + getItem(position));
		holder.imageRequest = ImageCacheManager.loadImage(getItem(position), ImageCacheManager
				.getImageListener(holder.imageView, mDefaultImageDrawable, mDefaultImageDrawable));
//		holder.imageView.setImageBitmap(holder.imageRequest.getBitmap());
		return convertView;

	}

	static class ViewHolder {
		ScaleImageView imageView;

		public ImageLoader.ImageContainer imageRequest;
	}
}
