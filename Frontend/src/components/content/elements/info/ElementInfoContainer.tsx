import React from 'react';

import { Link } from "react-router-dom";

export const ElementInfoContainer = ({ parent, match }) => {
	const info = parent.state.folders
					.concat(parent.state.files)
					.filter(element => element.id === match.params.id)[0];

	return <div className="element-info-container">
		<div className="dialog-header" style={{
			position: 'absolute',
			height: '40px',
			top: 0,
			right: 0,
			left: 0
		}}>
			<Link to="/">
				<button
					className="prev-button"
				>
					<i className="fas fa-chevron-left"/>
				</button>
			</Link>
			<span>{info.type.toLowerCase()} info</span>
		</div>
		<div className="element-info" style={{
			height: 'calc(100% - 60px)',
			width: '95%',
			bottom: '0px'
		}}>
			<div>
				<span className="description-text">Name</span>
				<span className="element-info-text">{info.name}</span>
			</div>
			<div>
				<span className="description-text">Location</span>
				<span className="element-info-text">{info.location}</span>
			</div>
			<div>
				<span className="description-text">Time created</span>
				<span className="element-info-text">{info.timeCreated}</span>
			</div>
			<div>
				<span className="description-text">Size</span>
				<span className="element-info-text">{info.size}</span>
			</div>
		</div>
	</div>
};
