package eu.faircode.backpacktrack2;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class GeonameAdapter extends ArrayAdapter<Geonames.Geoname> {
    private static final String TAG = "BPT2.Geonames";

    private Context context;
    private Location location;

    public GeonameAdapter(Context context, List<Geonames.Geoname> names, Location location) {
        super(context, 0, names);
        this.context = context;
        this.location = location;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Geonames.Geoname name = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.geoname, parent, false);

        ImageView ivFeature = (ImageView) convertView.findViewById(R.id.ivFeature);
        TextView tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        TextView tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
        TextView tvType = (TextView) convertView.findViewById(R.id.tvType);
        TextView tvPopulation = (TextView) convertView.findViewById(R.id.tvPopulation);

        int resId = 0;
        if (name.fcode != null)
            resId = context.getResources().getIdentifier(
                    "feature_" + name.fcode.toLowerCase() + "_60", "drawable", context.getPackageName());
        if (resId > 0)
            ivFeature.setImageResource(resId);
        ivFeature.setVisibility(resId > 0 ? View.VISIBLE : View.INVISIBLE);

        tvTitle.setText(name.name);
        tvDistance.setText((name.countryCode == null ? "" : name.countryCode + " ") + Math.round(name.location.distanceTo(location)) + " m");
        tvType.setText((name.fcode == null ? "" : name.fcode) + (name.fcodeName == null ? "" : " " + name.fcodeName));
        tvPopulation.setVisibility(name.population > 0 ? View.VISIBLE : View.GONE);
        tvPopulation.setText(Long.toString(name.population));

        return convertView;
    }
}

