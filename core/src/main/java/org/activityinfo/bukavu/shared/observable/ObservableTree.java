/*
 * Copyright 2014-2023 BeDataDriven Groep B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activityinfo.bukavu.shared.observable;

import com.google.gwt.core.client.Scheduler;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An observable that is a function of a tree of other observables.
 *
 * <p>To use this class, you must provide a {@link TreeLoader} implementation.</p>
 *
 * @param <KeyT> the type of a key that uniquely identifies tree nodes
 * @param <NodeT> the type of the tree nodes.
 * @param <TreeT> the type of the final tree constructed from the nodes.
 */
public final class ObservableTree<KeyT extends @NonNull Object, NodeT extends @NonNull Object, TreeT extends @NonNull Object> extends org.activityinfo.bukavu.shared.observable.Observable<TreeT> {

    private static final Logger LOGGER = Logger.getLogger(ObservableTree.class.getName());

    public interface TreeLoader<KeyT extends @NonNull Object, NodeT extends @NonNull Object, TreeT extends @NonNull Object> {

        /**
         *
         * @return the key of this tree's root node.
         */
        KeyT getRootKey();

        /**
         * @return the value of the node identified by {@code nodeKey}
         */
        org.activityinfo.bukavu.shared.observable.Observable<NodeT> get(KeyT nodeKey);

        /**
         *
         * @return the keys of the given {@code node}'s children.
         */
        Iterable<KeyT> getChildren(NodeT node);

        /**
         * Construct a tree from the set of loaded nodes.
         * @param nodes the nodes discovered in this tree. All nodes will be loaded when this method is called.
         * @return a new tree structure.
         */
        TreeT build(Map<KeyT, @Nullable NodeT> nodes);
    }

    private final TreeLoader<KeyT, NodeT, TreeT> loader;
    private final Scheduler scheduler;

    private final Map<KeyT, org.activityinfo.bukavu.shared.observable.Observable<NodeT>> nodes = new HashMap<>();
    private final Map<KeyT, @Nullable NodeT> loadedNodes = new HashMap<>();
    private final Map<KeyT, Subscription> subscriptions = new HashMap<>();

    private boolean crawling = false;
    private boolean crawlPending = false;

    public ObservableTree(TreeLoader<KeyT, NodeT, TreeT> loader, Scheduler scheduler) {
        this.loader = loader;
        this.scheduler = scheduler;
    }

    @Override
    protected void onConnect() {
        connectTo(loader.getRootKey());
        recrawl();
    }

    @Override
    protected void onDisconnect() {
        for (Subscription subscription : subscriptions.values()) {
            subscription.unsubscribe();
        }
        nodes.clear();
        loadedNodes.clear();
        subscriptions.clear();
    }

    private org.activityinfo.bukavu.shared.observable.Observable<NodeT> connectTo(KeyT nodeKey) {
        org.activityinfo.bukavu.shared.observable.Observable<NodeT> node = nodes.get(nodeKey);
        if(node == null) {
            node = loader.get(nodeKey);
            Subscription subscription = node.subscribe(new Observer<NodeT>() {

                private boolean onConnect = true;

                @Override
                public void onChange(@Nullable NodeT node) {
                    if(onConnect) {
                        loadedNodes.put(nodeKey, node);
                        onConnect = false;
                    } else {
                        ObservableTree.this.onNodeChanged(nodeKey, node);
                    }
                }
            });

            nodes.put(nodeKey, node);
            subscriptions.put(nodeKey, subscription);
        }
        return node;
    }

    private void disconnectFrom(KeyT nodeKey) {
        nodes.remove(nodeKey);
        loadedNodes.remove(nodeKey);
        Subscription subscription = subscriptions.remove(nodeKey);
        if(subscription != null) {
            subscription.unsubscribe();
        }
    }

    private void onNodeChanged(KeyT nodeKey, @Nullable NodeT node) {
        loadedNodes.put(nodeKey, node);
        if(node != null) {
            if(crawling) {
                crawlPending = true;
            } else {
                recrawl();
            }
        }
    }

    /**
     * Crawl from the root tree to all the leaves to find the set
     * of forms we need to query,
     */
    private void recrawl() {

        LOGGER.info("Tree " + loader + ": Recrawl starting...");

        Set<KeyT> reachable = new HashSet<>();
        Set<KeyT> loading = new HashSet<>();

        crawling = true;
        fireChange(null);

        try {

            crawl(loader.getRootKey(), reachable, loading);

            // First clean up forms that are no longer reachable
            List<KeyT> connectedForms = new ArrayList<>(nodes.keySet());
            for (KeyT nodeKey : connectedForms) {
                if (!reachable.contains(nodeKey)) {
                    disconnectFrom(nodeKey);
                }
            }

            LOGGER.info("Tree " + loader + ": reachable = " + reachable +
                    ", loading = " + loading);


            // Otherwise if we've got everything, we can build the tree
            if (loading.isEmpty()) {
                rebuildTree();
            }

            // Otherwise we have to wait for one of our pending nodes to load
            // and then we can recrawl.

        } finally {
            crawling = false;
        }

        // Was there a change to one of the nodes while we crawling?
        // Restart a crawl now

        if(crawlPending) {
            scheduler.scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    ObservableTree.this.recrawl();
                }
            });
            crawlPending = false;
        }
    }

    /**
     * Recursively search the tree of forms for those that are reachable, missing, and still loading.
     */
    private void crawl(KeyT parentKey, Set<KeyT> reachable, Set<KeyT> loading) {
        boolean seenForFirstTime = reachable.add(parentKey);

        if(!seenForFirstTime) {
            return;
        }

        Observable<NodeT> node = connectTo(parentKey);
        @Nullable NodeT loaded = loadedNodes.get(parentKey);

        if(loaded == null) {
            loading.add(parentKey);

        } else {
            for (KeyT childKey : loader.getChildren(loaded)) {
                crawl(childKey, reachable, loading);
            }
        }
    }

    /**
     * After we have a loaded copy of all the form schemas, build the form tree and fire listeners.
     */
    private void rebuildTree() {

        LOGGER.info("Tree " + loader + " complete!");

        try {
            fireChange(loader.build(loadedNodes));
        } catch (Error e) {
            LOGGER.log(Level.SEVERE, "Exception rebuilding tree", e);
        }
    }

}
