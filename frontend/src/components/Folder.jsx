import React from 'react';
import icon from '../images/folder.png'

const imgStyle = {
	height		: '40px',
	'width' 	: '50px',
	objectFit	: 'cover',
}

const Folder = props => {

	return (
		<div className="folder" onClick={props.whenClicked}>
			<div>
				<img src={icon} style={imgStyle} alt="???"/>	
			</div>
			<div>
				<span>{props.name}</span>
			</div>
		</div>
  	);
}

export default Folder;