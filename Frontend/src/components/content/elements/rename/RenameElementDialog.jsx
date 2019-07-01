import React from 'react';

import {Link} from 'react-router-dom';

const RenameElementDialog = props => (
    
    <div className="standard-dialog">
        <div className="dialog-header">
            <Link to="/">
                <button
                    className="prev-button"
                >
                    <i className="fas fa-chevron-left"/>
                </button>
            </Link>
            <i className="fas fa-folder"/>
            <span>Rename {props.element.name}</span>
        </div>
    <div 
        style={{ 
            padding: '10px',
            height: '70%',
            textAlign: 'center' 
        }}
    >
        <input
            defaultValue={props.element.name}
            placeholder="Name"
            id="folder-name"
            type="text"
            style={{
                margin: 'auto',
                marginTop: '20px',
                borderBottom: 'solid 1px grey',
                height: '30px',
                width: '90%'
            }}
        />
            <Link className="ok-button" onClick={() => props.onRename(document.getElementById('folder-name').value)} to={'/'}>
                Rename
            </Link>
        </div>
    </div>
);

export default RenameElementDialog;