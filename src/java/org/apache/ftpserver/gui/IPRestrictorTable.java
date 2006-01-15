// $Id$
/*
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ftpserver.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

/**
 * IP restrictor table.
 * 
 * @author <a href="mailto:rana_b@yahoo.com">Rana Bhattacharyya</a>
 */
public 
class IPRestrictorTable extends JPanel implements TableModel, ListSelectionListener {

    private static final long serialVersionUID = -7517200112683933776L;

    private final static String HEADER[] = {
            "IP Pattern",
            "Permission"
    };
    
    private Vector entries = new Vector();
    private EventListenerList modelListeners  = new EventListenerList();
    
    private JTable table;
    private JButton addButton;
    private JButton insertButton;
    private JButton removeButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    
    /**
     * Default constructor.
     */
    public IPRestrictorTable() {
        initComponents();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        
        setLayout(new BorderLayout());
        
        table = new JTable(this);
        table.setColumnSelectionAllowed(false);
        table.setRowSelectionAllowed(true);
        table.setRowHeight(20);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(this);
        table.getColumnModel().getSelectionModel().addListSelectionListener(this);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(buttonPanel, BorderLayout.SOUTH);
        
        addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addRow();
            }
        });
        buttonPanel.add(addButton);
        
        insertButton = new JButton("Insert");
        insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertRow();
            }
        });
        buttonPanel.add(insertButton);
        
        removeButton = new JButton("Remove");
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeRow();
            }
        });
        buttonPanel.add(removeButton);
        
        moveUpButton = new JButton("Move Up");
        moveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveUpRow();
            }
        });
        buttonPanel.add(moveUpButton);
        
        moveDownButton = new JButton("Move Down");
        moveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveDownRow();
            }
        });
        buttonPanel.add(moveDownButton);
        setButtonStatus();
    }
    
    /**
     * Set button enable/disable status
     */
    private void setButtonStatus() {
        addButton.setEnabled(canBeAdded());
        insertButton.setEnabled(canBeInserted());
        removeButton.setEnabled(canBeRemoved());
        moveUpButton.setEnabled(canBeMovedUp());
        moveDownButton.setEnabled(canBeMovedDown());
    }
    
    /**
     * Can a new row be added?
     */
    private boolean canBeAdded() {
        return true;
    }
    
    /**
     * Can a row be inserted?
     */
    private boolean canBeInserted() {
        return true;
    }
    
    /**
     * Can a row be removed?
     */
    private boolean canBeRemoved() {
        
        // no data - nothing to be removed
        if( (entries == null) || (entries.size() == 0) ) {
            return false;
        }
        
        // no row selection - nothing to be removed
        int selRow = table.getSelectedRow();
        return (selRow >= 0);
    }
    
    /**
     * Can the selected row be moved up?
     */
    private boolean canBeMovedUp() {
        
        // no data - nothing to move up
        if( (entries == null) || (entries.size() == 0) ) {
            return false;
        }
        
        // no selection or the first row has been selected
        int selRow = table.getSelectedRow();
        return (selRow > 0);
    }
    
    /**
     * Can the selected row be moved down?
     */
    private boolean canBeMovedDown() {
        
        // no data - nothing to move down
        if( (entries == null) || (entries.size() == 0) ) {
            return false;
        }
        
        // no selection - nothing to move down
        int selRow = table.getSelectedRow();
        if(selRow == -1) {
            return false;
        }
        
        // last row cannot be moved down
        return ( selRow != (entries.size()-1) );
    }
    
    /**
     * Add a new row.
     */
    public void addRow() {
        
        // new row cannot be added
        if(!canBeAdded()) {
            return;
        }
        
        // save the changed data if any 
        stopCellEditing();
        
        // get row data and add
        Entry entry = new Entry();
        entries.add(entry);
        
        // notify listeners and select the newly added row
        int lastRow = entries.size() - 1;
        fireTableChanged( new TableModelEvent(this, lastRow, lastRow,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT) );
        table.setRowSelectionInterval(lastRow, lastRow);
    }
    
    
    /**
     * Inser a new row entry an the selected (or last position)
     */
    public void insertRow() {
        
        // new row cannot be inserted
        if(!canBeInserted()) {
            return;
        }
        
        // save the changed data if any 
        stopCellEditing(); 
        
        // get the selected row 
        int selRow = table.getSelectedRow();
        if(selRow == -1) {
            selRow = entries.size() - 1;
        }
        if(selRow == -1) {
            selRow = 0;
        }
        
        // get row data and add
        Entry entry = new Entry();
        entries.add(selRow, entry);
        
        // notify listeners
        fireTableChanged( new TableModelEvent(this, selRow, selRow,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT) );
        table.setRowSelectionInterval(selRow, selRow);
    }
    
    /**
     * Delete the selected (or the last) row
     */
    public void removeRow() {
        
        // row cannot be removed
        if(!canBeRemoved()) {
            return;
        }
        
        // save the changed data if any 
        stopCellEditing();
        
        // get current selection
        int selRow = table.getSelectedRow();
        if(selRow == -1) {
            selRow = entries.size() - 1;
        }
        
        // remove row
        entries.remove(selRow);
        fireTableChanged( new TableModelEvent(this, selRow, selRow,
             TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE) );
        
        // select another row
        if(entries.isEmpty()) {
            setButtonStatus();
        }
        else {
            if(selRow == entries.size()) {
                --selRow;
            }
            table.setRowSelectionInterval(selRow, selRow);
        }
    }
    
    /**
     * Move up a row.
     */
    public void moveUpRow() {
        
        // row cannot be moved up
        if(!canBeMovedUp()) {
            return;
        }
        
        // save the changed data if any 
        stopCellEditing();
        
        // change the entry positions
        int selRow = table.getSelectedRow();
        Entry ent1 = (Entry)entries.get(selRow);
        Entry ent2 = (Entry)entries.get(selRow - 1);
        
        entries.set(selRow, ent2);
        entries.set(selRow - 1, ent1);
        
        // notify listeners and update the table selection
        fireTableChanged( new TableModelEvent(this, selRow - 1, selRow,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE) );
        table.setRowSelectionInterval(selRow - 1, selRow - 1);
    }
    
    /**
     * Move down a row.
     */
    public void moveDownRow() {
        
        // row cannot be moved down
        if(!canBeMovedDown()) {
            return;
        }
        
        // save the changed data if any 
        stopCellEditing();
        
        // change the entry positions
        int selRow = table.getSelectedRow();
        Entry ent1 = (Entry)entries.get(selRow);
        Entry ent2 = (Entry)entries.get(selRow + 1);
        
        entries.set(selRow, ent2);
        entries.set(selRow + 1, ent1);
        
        // notify listeners and update the table selection
        fireTableChanged( new TableModelEvent(this, selRow, selRow + 1,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE) );
        table.setRowSelectionInterval(selRow + 1, selRow + 1);
    }
    
    /**
     * Get data.
     */
    public Object[][] getData() {
        stopCellEditing();
        Object[][] retVal = new Object[entries.size()][2];
        for(int i=0; i<entries.size(); ++i) {
            Entry entry = (Entry)entries.get(i);
            retVal[i][0] = entry.pattern;
            retVal[i][1] = entry.allow;
        }
        return retVal;
    }
    
    /**
     * Set data.
     */
    public void setData(Object[][] objs) {
        cancelCellEditing();
        entries.clear();
        if(objs != null) {
            for(int i=0; i<objs.length; ++i) {
                Entry entry = new Entry();
                entry.pattern = (String)objs[i][0];
                entry.allow = (Boolean)objs[i][1];
                entries.add(entry);
            }
        }
        fireTableChanged( new TableModelEvent(this) );
        if(entries.isEmpty()) {
            setButtonStatus();
        }
        else {
            table.setRowSelectionInterval(0, 0);
        }
    }  
    
    /**
     * Cancel editing
     */
    public void cancelCellEditing() {
        if(table.isEditing()) {
            int row = table.getEditingRow();
            int col = table.getEditingColumn();
            if( (row != -1) && (col != -1) ) {
                TableCellEditor editor = table.getCellEditor();
                editor.cancelCellEditing();
            }
        }
    }
    
    /**
     * Stop editing
     */
    public void stopCellEditing() {
        if(table.isEditing()) {
            int row = table.getEditingRow();
            int col = table.getEditingColumn();
            if( (row != -1) && (col != -1) ) {
                TableCellEditor editor = table.getCellEditor();
                editor.stopCellEditing();
            }
        }
    }

    /**
     * If row selection changed - fire button status.
     */
    public void valueChanged(ListSelectionEvent e) {
        setButtonStatus();
    }
    
    ///////////////////////////////////////////////////////////////////////
    ///////////////////// Table Model Implementation //////////////////////
    /**
     * Add new listener.
     */
    public void addTableModelListener(TableModelListener l) {
        modelListeners.add(TableModelListener.class, l);
    }
    
    /**
     * Get column class.
     */
    public Class getColumnClass(int colIdx) {
        Class type = null;
        if(colIdx == 0) {
            type = String.class;
        }
        else {
            type = Boolean.class;
        }
        return type;
    }
    
    /**
     * Get column count.      
     */
    public int getColumnCount() {
        return HEADER.length;
    }
     
    /**
     * Get header name.
     */
    public String getColumnName(int col) {
        return HEADER[col];
    }
    
    /**
     * Get row count.
     */
    public int getRowCount() {
        return entries.size();
    }
    
    /**
     * Get value.
     */
    public Object getValueAt(int row, int col) {
        if(entries == null) {
            return null;
        }
        
        Entry entry = (Entry)entries.get(row);
        Object retVal = null;
        if(col == 0) {
            retVal = entry.pattern;
        }
        else {
            retVal = entry.allow;
        }
        return retVal;
    }
    
    /**
     * Cells are editable.
     */
    public boolean isCellEditable(int row, int col) {
        return true;
    }
    
    /**
     * Set value.
     */
    public void setValueAt(Object val, int row, int col) {
        if(entries == null) {
            return;
        }
        
        Entry entry = (Entry)entries.get(row);
        if(col == 0) {
            entry.pattern = (String)val;
        }
        else {
            entry.allow = (Boolean)val;
        }
    }
    
    /**
     * Remove listener.
     */
    public void removeTableModelListener(TableModelListener l) {
        modelListeners.remove(TableModelListener.class, l);
    }
    
    /**
     * Event handler.
     */
    private void fireTableChanged(TableModelEvent e) {
        Object[] listeners = modelListeners.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener)listeners[i+1]).tableChanged(e);
            }
        }
    }
            
    ///////////////////// Inner class to hold IP and permission //////////////////
    private static class Entry {
        public String pattern = "*";
        public Boolean allow = Boolean.TRUE;
    }
}
