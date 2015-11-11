package com.kickstarter.libs;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;

import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;

import rx.Subscription;
import rx.functions.Action0;

public final class Paginator {
  private @NonNull final RecyclerView recyclerView;
  private @NonNull final Action0 nextPage;
  private Subscription subscription = null;

  public Paginator(@NonNull final RecyclerView recyclerView, @NonNull final Action0 nextPage) {
    this.recyclerView = recyclerView;
    this.nextPage = nextPage;
    start();
  }

  /**
   * Begin listening to the recycler view scroll events to determine
   * when pagination should happen.
   */
  public void start() {
    stop();

    subscription = RxRecyclerView.scrollEvents(recyclerView)
      .map(__ -> recyclerView.getLayoutManager())
      .ofType(LinearLayoutManager.class)
      .map(this::displayedItemFromLinearLayout)
      .distinctUntilChanged()
      .filter(this::visibleItemIsCloseToBottom)
      .subscribe(__ -> nextPage.call());
  }

  /**
   * Stop listening to recycler view scroll events and discard the
   * associated resources. This should be done when the object that
   * created `this` is released.
   */
  public void stop() {
    if (subscription != null) {
      subscription.unsubscribe();
      subscription = null;
    }
  }

  /**
   * Returns a (visibleItem, totalItemCount) pair given a linear layout manager.
   */
  private @NonNull Pair<Integer, Integer> displayedItemFromLinearLayout(@NonNull final LinearLayoutManager manager) {
    final int visibleItemCount = manager.getChildCount();
    final int totalItemCount = manager.getItemCount();
    final int pastVisibleItems = manager.findFirstVisibleItemPosition();
    return new Pair<>(visibleItemCount + pastVisibleItems, totalItemCount);
  }

  private boolean visibleItemIsCloseToBottom(@NonNull final Pair<Integer, Integer> visibleItemOfTotal) {
    return visibleItemOfTotal.first == visibleItemOfTotal.second - 2;
  }
}