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
    
    private Vector m_entries = new Vector();
    private EventListenerList m_modelListeners  = new EventListenerList();
    
    private JTable m_table;
    private JButton m_addButton;
    private JButton m_insertButton;
    private JButton m_removeButton;
    private JButton m_moveUpButton;
    private JButton m_moveDownButton;
    
    /**
     * Default constructor
     */
    public IPRestrictorTable() {
        initComponents();
    }
    
    /**
     * Initialize UI components.
     */
    private void initComponents() {
        
        setLayout(new BorderLayout());
        
        m_table = new JTable(this);
        m_table.setColumnSelectionAllowed(false);
        m_table.setRowSelectionAllowed(true);
        m_table.setRowHeight(20);
        m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        m_table.getSelectionModel().addListSelectionListener(this);
        m_table.getColumnModel().getSelectionModel().addListSelectionListener(this);
        
        JScrollPane scrollPane = new JScrollPane(m_table);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        add(buttonPanel, BorderLayout.SOUTH);
        
        m_addButton = new JButton("Add");
        m_addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addRow();
            }
        });
        buttonPanel.add(m_addButton);
        
        m_insertButton = new JButton("Insert");
        m_insertButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                insertRow();
            }
        });
        buttonPanel.add(m_insertButton);
        
        m_removeButton = new JButton("Remove");
        m_removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeRow();
            }
        });
        buttonPanel.add(m_removeButton);
        
        m_moveUpButton = new JButton("Move Up");
        m_moveUpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveUpRow();
            }
        });
        buttonPanel.add(m_moveUpButton);
        
        m_moveDownButton = new JButton("Move Down");
        m_moveDownButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                moveDownRow();
            }
        });
        buttonPanel.add(m_moveDownButton);
        setButtonStatus();
    }
    
    /**
     * Set button enable/disable status
     */
    private void setButtonStatus() {
        m_addButton.setEnabled(canBeAdded());
        m_insertButton.setEnabled(canBeInserted());
        m_removeButton.setEnabled(canBeRemoved());
        m_moveUpButton.setEnabled(canBeMovedUp());
        m_moveDownButton.setEnabled(canBeMovedDown());
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
        if( (m_entries == null) || (m_entries.size() == 0) ) {
            return false;
        }
        
        // no row selection - nothing to be removed
        int selRow = m_table.getSelectedRow();
        return (selRow >= 0);
    }
    
    /**
     * Can the selected row be moved up?
     */
    private boolean canBeMovedUp() {
        
        // no data - nothing to move up
        if( (m_entries == null) || (m_entries.size() == 0) ) {
            return false;
        }
        
        // no selection or the first row has been selected
        int selRow = m_table.getSelectedRow();
        return (selRow > 0);
    }
    
    /**
     * Can the selected row be moved down?
     */
    private boolean canBeMovedDown() {
        
        // no data - nothing to move down
        if( (m_entries == null) || (m_entries.size() == 0) ) {
            return false;
        }
        
        // no selection - nothing to move down
        int selRow = m_table.getSelectedRow();
        if(selRow == -1) {
            return false;
        }
        
        // last row cannot be moved down
        return ( selRow != (m_entries.size()-1) );
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
        m_entries.add(entry);
        
        // notify listeners and select the newly added row
        int lastRow = m_entries.size() - 1;
        fireTableChanged( new TableModelEvent(this, lastRow, lastRow,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT) );
        m_table.setRowSelectionInterval(lastRow, lastRow);
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
        int selRow = m_table.getSelectedRow();
        if(selRow == -1) {
            selRow = m_entries.size() - 1;
        }
        if(selRow == -1) {
            selRow = 0;
        }
        
        // get row data and add
        Entry entry = new Entry();
        m_entries.add(selRow, entry);
        
        // notify listeners
        fireTableChanged( new TableModelEvent(this, selRow, selRow,
                TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT) );
        m_table.setRowSelectionInterval(selRow, selRow);
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
        int selRow = m_table.getSelectedRow();
        if(selRow == -1) {
            selRow = m_entries.size() - 1;
        }
        
        // remove row
        m_entries.remove(selRow);
        fireTableChanged( new TableModelEvent(this, selRow, selRow,
             TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE) );
        
        // select another row
        if(m_entries.isEmpty()) {
            setButtonStatus();
        }
        else {
            if(selRow == m_entries.size()) {
                --selRow;
            }
            m_table.setRowSelectionInterval(selRow, selRow);
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
        int selRow = m_table.getSelectedRow();
        Entry ent1 = (Entry)m_entries.get(selRow);
        Entry ent2 = (Entry)m_entries.get(selRow - 1);
        
        m_entries.set(selRow, ent2);
        m_entries.set(selRow - 1, ent1);
        
        // notify listeners and update the table selection
        fireTableChanged( new TableModelEvent(this, selRow - 1, selRow,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE) );
        m_table.setRowSelectionInterval(selRow - 1, selRow - 1);
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
        int selRow = m_table.getSelectedRow();
        Entry ent1 = (Entry)m_entries.get(selRow);
        Entry ent2 = (Entry)m_entries.get(selRow + 1);
        
        m_entries.set(selRow, ent2);
        m_entries.set(selRow + 1, ent1);
        
        // notify listeners and update the table selection
        fireTableChanged( new TableModelEvent(this, selRow, selRow + 1,
              TableModelEvent.ALL_COLUMNS, TableModelEvent.UPDATE) );
        m_table.setRowSelectionInterval(selRow + 1, selRow + 1);
    }
    
    /**
     * Get data.
     */
    public Object[][] getData() {
        stopCellEditing();
        Object[][] retVal = new Object[m_entries.size()][2];
        for(int i=0; i<m_entries.size(); ++i) {
            Entry entry = (Entry)m_entries.get(i);
            retVal[i][0] = entry.m_pattern;
            retVal[i][1] = entry.m_allow;
        }
        return retVal;
    }
    
    /**
     * Set data.
     */
    public void setData(Object[][] objs) {
        cancelCellEditing();
        m_entries.clear();
        if(objs != null) {
            for(int i=0; i<objs.length; ++i) {
                Entry entry = new Entry();
                entry.m_pattern = (String)objs[i][0];
                entry.m_allow = (Boolean)objs[i][1];
                m_entries.add(entry);
            }
        }
        fireTableChanged( new TableModelEvent(this) );
        if(m_entries.isEmpty()) {
            setButtonStatus();
        }
        else {
            m_table.setRowSelectionInterval(0, 0);
        }
    }  
    
    /**
     * Cancel editing
     */
    public void cancelCellEditing() {
        if(m_table.isEditing()) {
            int row = m_table.getEditingRow();
            int col = m_table.getEditingColumn();
            if( (row != -1) && (col != -1) ) {
                TableCellEditor editor = m_table.getCellEditor();
                editor.cancelCellEditing();
            }
        }
    }
    
    /**
     * Stop editing
     */
    public void stopCellEditing() {
        if(m_table.isEditing()) {
            int row = m_table.getEditingRow();
            int col = m_table.getEditingColumn();
            if( (row != -1) && (col != -1) ) {
                TableCellEditor editor = m_table.getCellEditor();
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
        m_modelListeners.add(TableModelListener.class, l);
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
        return m_entries.size();
    }
    
    /**
     * Get value.
     */
    public Object getValueAt(int row, int col) {
        if(m_entries == null) {
            return null;
        }
        
        Entry entry = (Entry)m_entries.get(row);
        Object retVal = null;
        if(col == 0) {
            retVal = entry.m_pattern;
        }
        else {
            retVal = entry.m_allow;
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
        if(m_entries == null) {
            return;
        }
        
        Entry entry = (Entry)m_entries.get(row);
        if(col == 0) {
            entry.m_pattern = (String)val;
        }
        else {
            entry.m_allow = (Boolean)val;
        }
    }
    
    /**
     * Remove listener.
     */
    public void removeTableModelListener(TableModelListener l) {
        m_modelListeners.remove(TableModelListener.class, l);
    }
    
    /**
     * Event handler.
     */
    private void fireTableChanged(TableModelEvent e) {
        Object[] listeners = m_modelListeners.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == TableModelListener.class) {
                ((TableModelListener)listeners[i+1]).tableChanged(e);
            }
        }
    }
            
    ///////////////////// Inner class to hold IP and permission //////////////////
    private 
    class Entry {
        public String m_pattern = "*";
        public Boolean m_allow = Boolean.TRUE;
    }
}
