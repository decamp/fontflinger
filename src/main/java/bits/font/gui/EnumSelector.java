/* 
 * Copyright (c) 2012, Philip DeCamp
 * Released under the BSD 2-Clause License
 * http://opensource.org/licenses/BSD-2-Clause 
 */ 
package bits.font.gui;

import java.awt.Container;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import bits.layout.LayoutAdapter;


/**
 * @author Philip DeCamp
 */
@SuppressWarnings( { "rawtypes" } )
class EnumSelector extends JPanel {

    private final Map<Enum,String> mValueMap;
    private final JLabel mLabel;
    private final JComboBox mCombo;
    private final JTextField mField;
    private final Document mDocument;

    private final TextHandler mTextHandler;
    
    
    EnumSelector(String label, Map<Enum,String> valueMap) {
        mLabel = new JLabel(label);
        mLabel.setHorizontalAlignment(JLabel.RIGHT);
        
        mValueMap = valueMap;
        Object[] v = new Object[valueMap.size() + 1];
        int i = 0;
        
        for(Enum n: mValueMap.keySet()) {
            v[i++] = n;
        }
        
        v[i] = "other";
        add(mLabel);
        mCombo = new JComboBox(v);
        mField = new JTextField(valueMap.values().iterator().next());
        
        add(mCombo);
        add(mField);
        
        setLayout(new Layout());
        mCombo.addItemListener(new ItemHandler());
        
        mDocument = mField.getDocument();
        mTextHandler = new TextHandler();
        mDocument.addDocumentListener(mTextHandler);
    }

    
    
    public String getText() {
        return mField.getText();
    }
    
    
    
    private final class Layout extends LayoutAdapter {
        
        public void layoutContainer(Container cont) {
            int w = getWidth();
            int h = getHeight();
            int MARGIN = 4;
            int x = 0;
            int ww = 50;
            
            mLabel.setBounds(x, 0, ww, h);
            x += ww + MARGIN;

            ww = (w - x - MARGIN * 2) * 2 / 3;
            mCombo.setBounds(x, 0, ww, h);
            x += ww + MARGIN;
            
            ww = (w - x - MARGIN);
            mField.setBounds(x, 0, ww, h); 
        }
        
    }

    
    private final class ItemHandler implements ItemListener {

        public void itemStateChanged(ItemEvent e) {
            Object obj = mCombo.getSelectedItem();
            String str = mValueMap.get(obj);
            
            if(str != null) {
                mDocument.removeDocumentListener(mTextHandler);
                mField.setText(str);
                mDocument.addDocumentListener(mTextHandler);
            }
        }
        
    }

    
    private final class TextHandler implements DocumentListener {
        
        public void changedUpdate(DocumentEvent e) {
            mCombo.setSelectedIndex(mCombo.getItemCount() - 1);
        }

        public void insertUpdate(DocumentEvent e) {
            mCombo.setSelectedIndex(mCombo.getItemCount() - 1);
        }

        public void removeUpdate(DocumentEvent e) {
            mCombo.setSelectedIndex(mCombo.getItemCount() - 1);
        }
        
    }
    
}
