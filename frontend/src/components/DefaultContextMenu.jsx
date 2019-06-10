import React from 'react';

const DefaultContextMenu = props => (

    <div
        className="context-menu"
        id="default-context-menu"
        style={{ 
            top : props.style.top,
            left : props.style.left
        }}
        >
        <div onClick={() => props.action("delete")}>
            <div>
                <i className="fas fa-trash"/>
            </div>
            <span>Delete All</span>
        </div>
        {props.parent.state.bufferElement !== undefined
            ?   <div onClick={() => props.action("delete")}>
                    <div>
                        <i class="fas fa-clipboard"/>
                    </div>
                    <span>Paste</span>
                </div>
            : undefined}
    </div>
);

export default DefaultContextMenu;