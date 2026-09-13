package com.wubitcode.androidapp4.ui

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wubitcode.androidapp4.R
import com.wubitcode.androidapp4.model.SubscriptionManager

/**
 * Displays all podcasts saved by the user.
 *
 * The list is refreshed whenever this Activity becomes visible
 * so newly added or removed subscriptions are shown immediately.
 */
class SubscriptionsActivity : AppCompatActivity() {

    private lateinit var recyclerViewSubscriptions: RecyclerView
    private lateinit var tvEmptySubscriptions: TextView
    private lateinit var subscriptionAdapter: SubscriptionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscriptions)

        setupToolbar()

        recyclerViewSubscriptions =
            findViewById(R.id.recyclerViewSubscriptions)

        tvEmptySubscriptions =
            findViewById(R.id.tvEmptySubscriptions)

        setupRecyclerView()
    }

    /**
     * Reload saved subscriptions whenever the user
     * returns to this screen.
     *
     * This is important because the user may unsubscribe
     * from a podcast on the details screen.
     */
    override fun onResume() {
        super.onResume()

        loadSubscriptions()
    }

    /**
     * Configures the toolbar and back navigation.
     */
    private fun setupToolbar() {

        val toolbar: Toolbar =
            findViewById(R.id.subscriptionsToolbar)

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = "My Subscriptions"
            setDisplayHomeAsUpEnabled(true)
        }

        toolbar.navigationIcon?.setTint(
            getColor(android.R.color.white)
        )
    }

    /**
     * Configures the RecyclerView used for saved podcasts.
     */
    private fun setupRecyclerView() {

        subscriptionAdapter =
            SubscriptionAdapter()

        recyclerViewSubscriptions.apply {

            layoutManager =
                LinearLayoutManager(
                    this@SubscriptionsActivity
                )

            adapter =
                subscriptionAdapter
        }
    }

    /**
     * Retrieves locally saved podcast subscriptions
     * and updates the visible list.
     */
    private fun loadSubscriptions() {

        val subscriptions =
            SubscriptionManager.getSubscriptions(this)

        subscriptionAdapter.updateSubscriptions(
            subscriptions
        )

        if (subscriptions.isEmpty()) {

            tvEmptySubscriptions.visibility =
                View.VISIBLE

            recyclerViewSubscriptions.visibility =
                View.GONE

        } else {

            tvEmptySubscriptions.visibility =
                View.GONE

            recyclerViewSubscriptions.visibility =
                View.VISIBLE
        }
    }

    /**
     * Returns to the previous screen when
     * the toolbar back arrow is selected.
     */
    override fun onSupportNavigateUp(): Boolean {

        finish()

        return true
    }
}