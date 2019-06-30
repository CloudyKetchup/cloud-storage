import React from 'react';

import {Link} from 'react-router-dom';

const DefaultContextMenu = props => {

    const pasteButton = () => {
        if (props.parent.state.bufferElement !== undefined) {
            return  <div onClick={() => props.action("paste")}>
                        <div className="context-menu-icon">
                            <i className="fas fa-clipboard"/>
                        </div>
                        <span>Paste</span>
                    </div>
        }
    };

    return (
        <div
            className="context-menu"
            id="default-context-menu"
            style={{ 
                top : props.style.top,
                left : props.style.left
            }}
            >
            <div onClick={() => props.action("upload-files")}>
                <div className="context-menu-icon">
                    <i className="fas fa-file-upload"/>
                </div>
                <span>Upload files</span>
            </div>
            <Link to="/create-folder">
                <div className="context-menu-icon">
                    <i className="fas fa-folder-plus"/>
                </div>
                <span>Create folder</span>
            </Link>
            <div onClick={() => props.action("delete-all")}>
                <div className="context-menu-icon">
                    <i className="far fa-trash-alt"/>
                </div>
                <span>Delete All</span>
            </div>
            {pasteButton()}
        </div>
    );
}

export default DefaultContextMenu;