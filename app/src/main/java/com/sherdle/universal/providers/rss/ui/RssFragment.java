package com.sherdle.universal.providers.rss.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.sherdle.universal.MainActivity;
import com.sherdle.universal.R;
import com.sherdle.universal.providers.rss.RSSFeed;
import com.sherdle.universal.providers.rss.RSSHandler;
import com.sherdle.universal.providers.rss.RSSItem;
import com.sherdle.universal.providers.rss.RssAdapter;
import com.sherdle.universal.util.Helper;
import com.sherdle.universal.util.InfiniteRecyclerViewAdapter;
import com.sherdle.universal.util.Log;
import com.sherdle.universal.util.ThemeUtils;
import com.sherdle.universal.util.ViewModeUtils;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * This activity is used to display a list of rss items
 */

public class RssFragment extends Fragment {

    private RSSFeed rssFeed = null;
    private ArrayList<RSSItem> postsList;
    private RssAdapter listAdapter;
    private ViewModeUtils viewModeUtils;
    private SwipeRefreshLayout swipeRefreshLayout;

    private Activity mAct;
    private RelativeLayout ll;
    private String url;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ll = (RelativeLayout) inflater.inflate(R.layout.fragment_list_refresh, container, false);
        return ll;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        RecyclerView listView = ll.findViewById(R.id.list);
        postsList = new ArrayList<>();
        listAdapter = new RssAdapter(getContext(), postsList);
        listAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        listView.setAdapter(listAdapter);
        listView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        swipeRefreshLayout = ll.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAct = getActivity();

        url = RssFragment.this.getArguments().getStringArray(MainActivity.FRAGMENT_DATA)[0];
        refreshItems();
    }

    private class RssTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                URL rssUrl = new URL(url);
                SAXParserFactory mySAXParserFactory = SAXParserFactory.newInstance();
                SAXParser mySAXParser = mySAXParserFactory.newSAXParser();
                XMLReader myXMLReader = mySAXParser.getXMLReader();
                RSSHandler myRSSHandler = new RSSHandler();
                myXMLReader.setContentHandler(myRSSHandler);
                InputSource myInputSource = new InputSource(rssUrl.openStream());
                myXMLReader.parse(myInputSource);

                rssFeed = myRSSHandler.getFeed();

            } catch (ParserConfigurationException | IOException | SAXException e) {
                Log.printStackTrace(e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (rssFeed != null) {
                if (rssFeed.getList().size() > 0) {
                    postsList.addAll(rssFeed.getList());
                }

                listAdapter.setHasMore(false);
                listAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_LIST);
                swipeRefreshLayout.setRefreshing(false);

            } else {
                String message = null;
                if (!url.startsWith("http"))
                    message = "Debug info: '" + url + "' is most likely not a valid RSS url. Make sure the url entered in your configuration starts with 'http' and verify if it's valid XML using https://validator.w3.org/feed/";
                Helper.noConnection(mAct, message);

                listAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_EMPTY);
                swipeRefreshLayout.setRefreshing(false);

            }

            super.onPostExecute(result);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.rss_menu, menu);
        viewModeUtils = new ViewModeUtils(getContext(), getClass());
        viewModeUtils.inflateOptionsMenu(menu, inflater);
        ThemeUtils.tintAllIcons(menu, mAct);
    }

    private void refreshItems() {
        postsList.clear();
        listAdapter.setModeAndNotify(InfiniteRecyclerViewAdapter.MODE_PROGRESS);
        new RssTask().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        viewModeUtils.handleSelection(item, new ViewModeUtils.ChangeListener() {
            @Override
            public void modeChanged() {
                listAdapter.notifyDataSetChanged();
            }
        });
        switch (item.getItemId()) {
            case R.id.info:
                //show information about the feed in general in a dialog
                if (rssFeed != null) {
                    String FeedTitle = (rssFeed.getTitle());
                    String FeedDescription = (rssFeed.getDescription());
                    //String FeedPubdate = (myRssFeed.getPubdate()); most times not present
                    String FeedLink = (rssFeed.getLink());

                    AlertDialog.Builder builder = new AlertDialog.Builder(mAct);

                    String titlevalue = getResources().getString(R.string.feed_title_value);
                    String descriptionvalue = getResources().getString(R.string.feed_description_value);
                    String linkvalue = getResources().getString(R.string.feed_link_value);

                    if (FeedLink.equals("")) {
                        builder.setMessage(titlevalue + ": \n" + FeedTitle +
                                "\n\n" + descriptionvalue + ": \n" + FeedDescription);
                    } else {
                        builder.setMessage(titlevalue + ": \n" + FeedTitle +
                                "\n\n" + descriptionvalue + ": \n" + FeedDescription +
                                "\n\n" + linkvalue + ": \n" + FeedLink);
                    }

                    builder.setNegativeButton(getResources().getString(R.string.ok), null)
                            .setCancelable(true);
                    builder.create();
                    builder.show();

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}