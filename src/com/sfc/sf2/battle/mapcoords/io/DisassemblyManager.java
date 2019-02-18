/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sfc.sf2.battle.mapcoords.io;

import com.sfc.sf2.graphics.Tile;
import com.sfc.sf2.battle.mapcoords.BattleMapCoords;
import com.sfc.sf2.map.block.MapBlock;
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static com.sfc.sf2.graphics.compressed.StackGraphicsEncoder.bytesToHex;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author wiz
 */
public class DisassemblyManager {
    
    private static final String MACRO_LABEL = "battleMapCoords";
    
    private static String header;
    
    public BattleMapCoords[] importDisassembly(String mapCoordsPath){
        System.out.println("com.sfc.sf2.battlemapcoords.io.DisassemblyManager.importDisassembly() - Importing disassembly ...");
        BattleMapCoords[] coords = null;
        if(mapCoordsPath.endsWith(".asm")){
            coords = importDisassemblyAsm(mapCoordsPath);
        }else{
            coords = importDisassemblyBin(mapCoordsPath);
        }
        System.out.println("com.sfc.sf2.battle.mapcoords.io.DisassemblyManager.importDisassembly() - Disassembly imported.");
        return coords;
    }      
    
    public BattleMapCoords[] importDisassemblyAsm(String mapCoordsPath){
        System.out.println("com.sfc.sf2.battlemapcoords.io.DisassemblyManager.importDisassembly() - Importing disassembly ...");
        BattleMapCoords[] coords = null;
        List<BattleMapCoords> coordsList = new ArrayList();
        try{
            File file = new File(mapCoordsPath);
            Scanner scan = new Scanner(file);
            boolean inHeader = true;
            header = "";
            while(scan.hasNext()){
                String line = scan.nextLine();
                if(line.trim().startsWith(MACRO_LABEL)){
                    inHeader = false;
                    /*  ; Map, X, Y, Width, Height, Trigger X, Trigger Y
                        battleMapCoords 63, 0, 12, 32, 36, 255, 255
                    */
                    String[] params = line.trim().substring(MACRO_LABEL.length()).trim().split(",");
                    int map = Integer.valueOf(params[0].trim());
                    int x = Integer.valueOf(params[1].trim());
                    int y = Integer.valueOf(params[2].trim());
                    int width = Integer.valueOf(params[3].trim());
                    int height = Integer.valueOf(params[4].trim());
                    int trigX = Integer.valueOf(params[5].trim());
                    int trigY = Integer.valueOf(params[6].trim());
                    BattleMapCoords entry = new BattleMapCoords();
                    entry.setMap(map);
                    entry.setX(x);
                    entry.setY(y);
                    entry.setWidth(width);
                    entry.setHeight(height);
                    entry.setTrigX(trigX);
                    entry.setTrigY(trigY);
                    coordsList.add(entry);
                }else{
                    if(inHeader){
                        header+=line;
                        header+="\n";
                    }
                }
            }          
        }catch(Exception e){
             System.err.println("com.sfc.sf2.battle.mapcoords.io.DisassemblyManager.importDisassembly() - Error while parsing graphics data : "+e);
             e.printStackTrace();
        }    
        coords = new BattleMapCoords[coordsList.size()];
        coords = coordsList.toArray(coords);
        System.out.println("com.sfc.sf2.battle.mapcoords.io.DisassemblyManager.importDisassembly() - Disassembly imported.");
        return coords;
    }      
    
    public BattleMapCoords[] importDisassemblyBin(String mapCoordsPath){
        System.out.println("com.sfc.sf2.battlemapcoords.io.DisassemblyManager.importDisassembly() - Importing disassembly ...");
        BattleMapCoords[] coords = null;
        List<BattleMapCoords> coordsList = new ArrayList();
        try{
            Path path = Paths.get(mapCoordsPath);
            if(path.toFile().exists()){
                byte[] data = Files.readAllBytes(path);
                int cursor = 0;
                while((cursor+6)<data.length){
                    BattleMapCoords entry = new BattleMapCoords();
                    entry.setMap(getByte(data,cursor));
                    entry.setX(getByte(data,cursor+1));
                    entry.setY(getByte(data,cursor+2));
                    entry.setWidth(getByte(data,cursor+3));
                    entry.setHeight(getByte(data,cursor+4));
                    entry.setTrigX(getByte(data,cursor+5));
                    entry.setTrigY(getByte(data,cursor+6));
                    coordsList.add(entry);
                    cursor+=7;
                }
            }            
        }catch(Exception e){
             System.err.println("com.sfc.sf2.battle.mapcoords.io.DisassemblyManager.importDisassembly() - Error while parsing graphics data : "+e);
             e.printStackTrace();
        }    
        coords = new BattleMapCoords[coordsList.size()];
        coords = coordsList.toArray(coords);
        System.out.println("com.sfc.sf2.battle.mapcoords.io.DisassemblyManager.importDisassembly() - Disassembly imported.");
        return coords;
    }      

    private static short getWord(byte[] data, int cursor){
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data[cursor+1]);
        bb.put(data[cursor]);
        short s = bb.getShort(0);
        return s;
    }
    
    private static byte getByte(byte[] data, int cursor){
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(data[cursor]);
        byte b = bb.get(0);
        return b;
    }      
    
    public void exportDisassembly(BattleMapCoords[] coords, String coordsFilePath){
        System.out.println("com.sfc.sf2.battlemapcoords.io.DisassemblyManager.exportDisassembly() - Exporting disassembly ...");
        try { 
            if(coordsFilePath.endsWith(".asm")){
                StringBuilder asm = new StringBuilder();
                asm.append(header);
                asm.append(produceCoordsAsm(coords));
                Path coordsPath = Paths.get(coordsFilePath);
                Files.write(coordsPath,asm.toString().getBytes());
                System.out.println(asm);
            }else{
                byte[] coordsBytes = produceCoordsBytes(coords);
                Path coordsPath = Paths.get(coordsFilePath);
                Files.write(coordsPath,coordsBytes);
                System.out.println(coordsBytes.length + " bytes into " + coordsFilePath);
            }
        } catch (Exception ex) {
            Logger.getLogger(DisassemblyManager.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            System.out.println(ex);
        }            
        System.out.println("com.sfc.sf2.battlemapcoords.io.DisassemblyManager.exportDisassembly() - Disassembly exported.");        
    }     
   
    private static byte[] produceCoordsBytes(BattleMapCoords[] coords){
        byte[] coordsBytes = new byte[coords.length*7];
        for(int i=0;i<coords.length;i++){
            BattleMapCoords coord = coords[i];
            coordsBytes[i*7+0] = (byte)coord.getMap();
            coordsBytes[i*7+1] = (byte)coord.getX();
            coordsBytes[i*7+2] = (byte)coord.getY();
            coordsBytes[i*7+3] = (byte)coord.getWidth();
            coordsBytes[i*7+4] = (byte)coord.getHeight();
            coordsBytes[i*7+5] = (byte)coord.getTrigX();
            coordsBytes[i*7+6] = (byte)coord.getTrigY();
        }
        return coordsBytes;
    }   
   
    private static String produceCoordsAsm(BattleMapCoords[] coords){
        StringBuilder asm = new StringBuilder();
        for(int i=0;i<coords.length;i++){
            BattleMapCoords coord = coords[i];
            asm.append("                "+MACRO_LABEL+" "+coord.getMap()+", "+coord.getX()+", "+coord.getY()+", "+coord.getWidth()+", "+coord.getHeight()+", "+coord.getTrigX()+", "+coord.getTrigY()+"\n");
        }
        return asm.toString();
    }    
 
    
    
    
}
