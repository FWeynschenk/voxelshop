package com.vitco.util.action;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * maps strings to actions, allows for checks (unused actions, undeclared actions)
 */
public class ActionManager implements ActionManagerInterface {

    // maps strings to action
    private final Map<String, AbstractAction> map = new HashMap<String, AbstractAction>();

    // maps action names to threads
    private final Map<String, ArrayList<Runnable>> actionQueStack = new HashMap<String, ArrayList<Runnable>>();

    @Override
    public void performWhenActionIsReady(String action, Runnable thread) {
        if (map.containsKey(action)) {
            thread.run(); // the action is already ready
        } else {
            // we need to wait till the action is ready
            ArrayList<Runnable> value;
            if (actionQueStack.containsKey(action)) {
                value = actionQueStack.get(action);
            } else {
                value = new ArrayList<Runnable>();
            }
            value.add(thread);
            actionQueStack.put(action, value);
        }
    }

    // allows to register an action
    @Override
    public void registerAction(String key, AbstractAction action) {
        if (map.containsKey(key)) {
            System.err.println("Error: The action \"" + key + "\" is already registered!");
        } else {
            map.put(key, action);
            if (actionQueStack.containsKey(key)) { // run the thread that was waiting for this action
                ArrayList<Runnable> value = actionQueStack.get(key);
                for (Runnable thread : value) {
                    // note: this can create an error if the action is not
                    // the extended type of AbstractAction that was
                    // expected (e.g. dummy action)
                    thread.run();
                }
                value.clear(); // empty array list (unnecessary?)
                actionQueStack.remove(key); // free the key
            }
        }

    }

    // allows to retrieve an action for a key
    public AbstractAction getAction(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            System.err.println("Error: The action \"" + key + "\" is not registered!");
            return null;
        }
    }

    // ===========================
    // BELOW ERROR CHECKING

    // holds action names, to detect possible errors
    private final ArrayList<String> actionNames = new ArrayList<String>();
    // validate things
    @Override
    public boolean performValidityCheck() {
        boolean result = true;
        for (final String actionName : actionNames) {
            if (!map.containsKey(actionName)) {
                System.err.println("Error: The action \"" + actionName + "\" is not registered!");
                System.err.println("Creating dummy action.");
                // register dummy action
                registerAction(actionName, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("Dummy Action \"" + actionName + "\"");
                    }
                });
                result = false;
            }
        }
        for (String key : map.keySet()) {
            if (!actionNames.contains(key)) {
                System.err.println("Error: The action \"" + key + "\" is never used!");
            }
        }
        // this should always be empty as a dummy action is registered above
        assert actionQueStack.size() == 0;
        for (String key : actionQueStack.keySet()) {
            System.err.println("Error: The action \"" + key + "\" was never registered!");
        }
        return result;
    }
    @Override
    public void registerActionName(String key) {
        // only need to register this action name one (several pieces of code
        // might be executing this action!)
        if (!actionNames.contains(key)) {
            actionNames.add(key);
        }
    }
}