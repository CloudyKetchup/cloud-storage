import React from 'react';

const ElementInfoContainer = props => (

	<div className="element-info-container">
		<div className="dialog-header" style={{
			position: 'absolute',
		    height 	: '40px',
		    top		: 0,
		    right	: 0,
		    left 	: 0
		}}>
			<button
				className="prev-button"
				onClick={() => props.parent.setState({ elementInfoContainer : false })}
			>
				<i className="fas fa-chevron-left"/>
			</button>
			<span>{props.data.type.toLowerCase()} info</span>
		</div>
		<div className="element-info" style={{
		    height	: 'calc(100% - 30px)',
		    width	: '100%',
		    bottom	: 0
		}}>
			<div>
				<span className="description-text">Name</span>
				<span className="element-info-text">{props.data.name}</span>
			</div>
			<div>
				<span className="description-text">Path</span>
				<span className="element-info-text">{props.data.path}</span>
			</div>
			<div>
				<span className="description-text">Location</span>
				<span className="element-info-text">{props.data.location}</span>
			</div>
			<div>
				<span className="description-text">Time created</span>
				<span className="element-info-text">{props.data.timeCreated}</span>
			</div>
			<div>
				<span className="description-text">Size</span>
				<span className="element-info-text">{props.data.size}</span>
			</div>
		</div>
	</div>
);

export default ElementInfoContainer;