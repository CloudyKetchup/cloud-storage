import React from 'react'

const BufferElementIndicator = props => (
    
    <div className="buffer-element">
        <div className="buffer-element-indicator">
        	<i className="fas fa-paste"/>
        </div>
        <div className="buffer-element-name">
            <span>{props.element.data.name}</span>
        </div>
    </div>
);

export default BufferElementIndicator;