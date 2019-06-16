import React from 'react';

const RenameElementDialog = props => {

    const element = props.parent.state.elementSelected;

    return (
        <div className="standard-dialog">
            <div className="dialog-header">
                <button
                    className="prev-button"
                    onClick={() => props.parent.setState({ renameElementDialog : false })}
                >
                    <i className="fas fa-chevron-left"/>
                </button>
                <i className="fas fa-folder"/>
                <span>Rename {element.name}</span>
            </div>
        <div 
            className="container"
            style={{ textAlign : 'center' }}
        >
            <div className="input-field col s6">
                <input
                    defaultValue={element.name}
                    placeholder="Name"
                    id="folder-name"
                    type="text"
                />
            </div>
                <button 
                    className="ok-button"
                    onClick={() => props.onRename(document.getElementById('folder-name').value)}
                >Rename</button>
            </div>
        </div>
    );
}

export default RenameElementDialog;