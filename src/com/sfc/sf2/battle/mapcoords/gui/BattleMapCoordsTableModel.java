/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.battle.mapcoords.gui;

import com.sfc.sf2.battle.mapcoords.BattleMapCoords;
import com.sfc.sf2.battle.mapcoords.layout.BattleMapCoordsLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author wiz
 */
public class BattleMapCoordsTableModel extends AbstractTableModel {
    
    private final Integer[][] tableData;
    private final String[] columns = {"Map", "X", "Y", "Width", "Height", "Trigger X", "Trigger Y"};
    private BattleMapCoords[] coords;
    private BattleMapCoordsLayout layout;
    
    public BattleMapCoordsTableModel(BattleMapCoords[] coords, BattleMapCoordsLayout layout) {
        super();
        this.coords = coords;
        this.layout = layout;
        tableData = new Integer[128][];
        int i = 0;
        if(coords!=null){
            while(i<coords.length){
                tableData[i] = new Integer[7];
                tableData[i][0] = coords[i].getMap();
                tableData[i][1] = coords[i].getX();
                tableData[i][2] = coords[i].getY();
                tableData[i][3] = coords[i].getWidth();
                tableData[i][4] = coords[i].getHeight();
                tableData[i][5] = coords[i].getTrigX();
                tableData[i][6] = coords[i].getTrigY();
                i++;
            }
        }
        while(i<tableData.length){
            tableData[i] = new Integer[7];
            i++;
        }
    }
    
    public void updateProperties() {
        List<BattleMapCoords> entries = new ArrayList<>();
        for(Integer[] entry : tableData){
            if(entry[0] != null){
                BattleMapCoords coordsEntry = new BattleMapCoords();
                coordsEntry.setMap(entry[0]);
                if(entry[1]==null){entry[1]=0;}
                coordsEntry.setX(entry[1]);
                if(entry[2]==null){entry[2]=0;}
                coordsEntry.setY(entry[2]);
                if(entry[3]==null){entry[3]=63;}
                coordsEntry.setWidth(entry[3]);
                if(entry[4]==null){entry[4]=63;}
                coordsEntry.setHeight(entry[4]);
                if(entry[5]==null){entry[5]=-1;}
                coordsEntry.setTrigX(entry[5]);
                if(entry[6]==null){entry[6]=-1;}
                coordsEntry.setTrigY(entry[6]);           
                entries.add(coordsEntry);
            }
        }
        BattleMapCoords[] newCoords = new BattleMapCoords[entries.size()];
        this.coords = entries.toArray(newCoords);
    }
    
    @Override
    public Class getColumnClass(int column) {
        return Integer.class;
    }    
    
    @Override
    public Object getValueAt(int row, int col) {
        return tableData[row][col];
    }
    @Override
    public void setValueAt(Object value, int row, int col) {
        tableData[row][col] = (Integer)value;
        updateProperties();
        if(row<coords.length){
            layout.setCoords(coords[row]);
            layout.updateCoordsDisplay();
            layout.revalidate();
            layout.repaint();
        }
    }    
 
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }    
    
    @Override
    public int getRowCount() {
        return tableData.length;
    }
 
    @Override
    public int getColumnCount() {
        return columns.length;
    }
 
    @Override
    public String getColumnName(int columnIndex) {
        return columns[columnIndex];
    }    

    public BattleMapCoords[] getCoords() {
        return coords;
    }

    public void setCoords(BattleMapCoords[] coords) {
        this.coords = coords;
    }
}
