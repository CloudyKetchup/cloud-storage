import React from 'react';

const DefaultContextMenu = props => {

    const pasteButton = () => {
        if (props.parent.state.bufferElement !== undefined) {
            return  <div onClick={() => props.action("paste")}>
                        <div>
                            <i className="fas fa-clipboard"/>
                        </div>
                        <span>Paste</span>
                    </div>
        }
    }

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
                <div>
                    <i className="fas fa-file-upload"/>
                </div>
                <span>Upload files</span>
            </div>
            <div onClick={() => props.action("create-folder")}>
                <div>
                    <i className="fas fa-folder-plus"/>
                </div>
                <span>Create folder</span>
            </div>
            <div onClick={() => props.action("delete")}>
                <div>
                    <i className="far fa-trash-alt"/>
                </div>
                <span>Delete All</span>
            </div>
            {pasteButton()}
        </div>
    );
}

export default DefaultContextMenu;