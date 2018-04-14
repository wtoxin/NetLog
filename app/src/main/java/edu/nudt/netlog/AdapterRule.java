package edu.nudt.netlog;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class AdapterRule extends RecyclerView.Adapter<AdapterRule.ViewHolder> implements Filterable {
    private static final String TAG = "NetLog.Adapter";

    private View anchor;
    private LayoutInflater inflater;
    private RecyclerView rv;
    private int colorText;
    private int colorChanged;
    private int colorOn;
    private int colorOff;
    private int colorGrayed;
    private int iconSize;
    private boolean wifiActive = true;
    private boolean otherActive = true;
    private boolean live = true;
    private List<Rule> listAll = new ArrayList<>();
    private List<Rule> listFiltered = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public View view;

        public LinearLayout llApplication;
        public ImageView ivIcon;
        public ImageView ivExpander;
        public TextView tvName;

        public LinearLayout llConfiguration;
        public TextView tvUid;
        public TextView tvPackage;
        public TextView tvVersion;
        public TextView tvInternet;
        public TextView tvDisabled;

//        public ImageButton ibSettings;
//        public ImageButton ibLaunch;

        public LinearLayout llFilter;
//        public ImageView ivLive;
        public ListView lvAccess;
        public ImageButton btnClearAccess;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            llApplication = itemView.findViewById(R.id.llApplication);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            ivExpander = itemView.findViewById(R.id.ivExpander);
            tvName = itemView.findViewById(R.id.tvName);

            llConfiguration = itemView.findViewById(R.id.llConfiguration);
            tvUid = itemView.findViewById(R.id.tvUid);
            tvPackage = itemView.findViewById(R.id.tvPackage);
            tvVersion = itemView.findViewById(R.id.tvVersion);
            tvInternet = itemView.findViewById(R.id.tvInternet);
            tvDisabled = itemView.findViewById(R.id.tvDisabled);

//            ibSettings = itemView.findViewById(R.id.ibSettings);
//            ibLaunch = itemView.findViewById(R.id.ibLaunch);

            llFilter = itemView.findViewById(R.id.llFilter);
//            ivLive = itemView.findViewById(R.id.ivLive);
            lvAccess = itemView.findViewById(R.id.lvAccess);
            btnClearAccess = itemView.findViewById(R.id.btnClearAccess);
        }
    }

    public AdapterRule(Context context, View anchor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        this.anchor = anchor;
        this.inflater = LayoutInflater.from(context);

        if (prefs.getBoolean("dark_theme", false))
            colorChanged = Color.argb(128, Color.red(Color.DKGRAY), Color.green(Color.DKGRAY), Color.blue(Color.DKGRAY));
        else
            colorChanged = Color.argb(128, Color.red(Color.LTGRAY), Color.green(Color.LTGRAY), Color.blue(Color.LTGRAY));

        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.textColorPrimary});
        try {
            colorText = ta.getColor(0, 0);
        } finally {
            ta.recycle();
        }

        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorOn, tv, true);
        colorOn = tv.data;
        context.getTheme().resolveAttribute(R.attr.colorOff, tv, true);
        colorOff = tv.data;

        colorGrayed = ContextCompat.getColor(context, R.color.colorGrayed);

        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeight, typedValue, true);
        int height = TypedValue.complexToDimensionPixelSize(typedValue.data, context.getResources().getDisplayMetrics());
        this.iconSize = Math.round(height * context.getResources().getDisplayMetrics().density + 0.5f);

        setHasStableIds(true);
    }

    public void set(List<Rule> listRule) {
        listAll = listRule;
        listFiltered = new ArrayList<>();
        listFiltered.addAll(listRule);
        notifyDataSetChanged();
    }

    public void setWifiActive() {
        wifiActive = true;
        otherActive = false;
        notifyDataSetChanged();
    }

    public void setMobileActive() {
        wifiActive = false;
        otherActive = true;
        notifyDataSetChanged();
    }

    public void setDisconnected() {
        wifiActive = false;
        otherActive = false;
        notifyDataSetChanged();
    }

    public boolean isLive() {
        return this.live;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        rv = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        rv = null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Context context = holder.itemView.getContext();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Get rule
        final Rule rule = listFiltered.get(position);

        // Handle expanding/collapsing
        holder.llApplication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rule.expanded = !rule.expanded;
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        // Show if non default rules
        holder.itemView.setBackgroundColor(rule.changed ? colorChanged : Color.TRANSPARENT);

        // Show expand/collapse indicator
        holder.ivExpander.setImageLevel(rule.expanded ? 1 : 0);

        // Show application icon
        if (rule.icon <= 0)
            holder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
        else {
            Uri uri = Uri.parse("android.resource://" + rule.packageName + "/" + rule.icon);
            GlideApp.with(holder.itemView.getContext())
                    .applyDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565))
                    .load(uri)
                    //.diskCacheStrategy(DiskCacheStrategy.NONE)
                    //.skipMemoryCache(true)
                    .override(iconSize, iconSize)
                    .into(holder.ivIcon);
        }

        // Show application label
        holder.tvName.setText(rule.name);

        // Show application state
        int color = rule.system ? colorOff : colorText;
        if (!rule.internet || !rule.enabled)
            color = Color.argb(128, Color.red(color), Color.green(color), Color.blue(color));
        holder.tvName.setTextColor(color);

        // Lockdown settings


        // Wi-Fi settings


        // Mobile settings

        // Expanded configuration section
        holder.llConfiguration.setVisibility(rule.expanded ? View.VISIBLE : View.GONE);

        // Show application details
        holder.tvUid.setText(Integer.toString(rule.uid));
        holder.tvPackage.setText(rule.packageName);
        holder.tvVersion.setText(rule.version);

        // Show application state
        holder.tvInternet.setVisibility(rule.internet ? View.GONE : View.VISIBLE);
        holder.tvDisabled.setVisibility(rule.enabled ? View.GONE : View.VISIBLE);


//        // Launch application settings
//        if (rule.expanded) {
//            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//            intent.setData(Uri.parse("package:" + rule.packageName));
//            final Intent settings = (intent.resolveActivity(context.getPackageManager()) == null ? null : intent);
//
////            holder.ibSettings.setVisibility(settings == null ? View.GONE : View.VISIBLE);
//            holder.ibSettings.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    context.startActivity(settings);
//                }
//            });
//        } else
//            holder.ibSettings.setVisibility(View.GONE);

//        // Launch application
//        if (rule.expanded) {
//            Intent intent = context.getPackageManager().getLaunchIntentForPackage(rule.packageName);
//            final Intent launch = (intent == null ||
//                    intent.resolveActivity(context.getPackageManager()) == null ? null : intent);
//
////            holder.ibLaunch.setVisibility(launch == null ? View.GONE : View.VISIBLE);
//            holder.ibLaunch.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    context.startActivity(launch);
//                }
//            });
//        } else
//            holder.ibLaunch.setVisibility(View.GONE);


        // Show Wi-Fi screen on condition

        // Show mobile screen on condition

        // Show roaming condition

        // Show lockdown

        // Reset rule


        holder.llFilter.setVisibility(Util.canFilter(context) ? View.VISIBLE : View.GONE);

        // Live
//        holder.ivLive.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                live = !live;
//                TypedValue tv = new TypedValue();
//                view.getContext().getTheme().resolveAttribute(live ? R.attr.iconPause : R.attr.iconPlay, tv, true);
//                holder.ivLive.setImageResource(tv.resourceId);
//                if (live)
//                    AdapterRule.this.notifyDataSetChanged();
//            }
//        });

        // Show logging/filtering is disabled

        // Show access rules
        if (rule.expanded) {
            // Access the database when expanded only
            final AdapterAccess badapter = new AdapterAccess(context,
                    DatabaseHelper.getInstance(context).getAccess(rule.uid));
            holder.lvAccess.setAdapter(badapter);
        } else {
            holder.lvAccess.setAdapter(null);
            holder.lvAccess.setOnItemClickListener(null);
        }

        // Clear access log
        holder.btnClearAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseHelper.getInstance(context).clearAccess(rule.uid, true);
                if (rv != null)
                    rv.scrollToPosition(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);

        //Context context = holder.itemView.getContext();
        //GlideApp.with(context).clear(holder.ivIcon);

        CursorAdapter adapter = (CursorAdapter) holder.lvAccess.getAdapter();
        if (adapter != null) {
            Log.i(TAG, "Closing access cursor");
            adapter.changeCursor(null);
            holder.lvAccess.setAdapter(null);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence query) {
                List<Rule> listResult = new ArrayList<>();
                if (query == null)
                    listResult.addAll(listAll);
                else {
                    query = query.toString().toLowerCase().trim();
                    int uid;
                    try {
                        uid = Integer.parseInt(query.toString());
                    } catch (NumberFormatException ignore) {
                        uid = -1;
                    }
                    for (Rule rule : listAll)
                        if (rule.uid == uid ||
                                rule.packageName.toLowerCase().contains(query) ||
                                (rule.name != null && rule.name.toLowerCase().contains(query)))
                            listResult.add(rule);
                }

                FilterResults result = new FilterResults();
                result.values = listResult;
                result.count = listResult.size();
                return result;
            }

            @Override
            protected void publishResults(CharSequence query, FilterResults result) {
                listFiltered.clear();
                if (result == null)
                    listFiltered.addAll(listAll);
                else {
                    listFiltered.addAll((List<Rule>) result.values);
                    if (listFiltered.size() == 1)
                        listFiltered.get(0).expanded = true;
                }
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public AdapterRule.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.rule, parent, false));
    }

    @Override
    public long getItemId(int position) {
        Rule rule = listFiltered.get(position);
        return rule.packageName.hashCode() * 100000L + rule.uid;
    }

    @Override
    public int getItemCount() {
        return listFiltered.size();
    }
}
