import React from 'react';
import icon from '../images/file.png'

const imgStyle = {
	height		: '40px',
	objectFit	: 'cover',
}

const File = props => {

	return (
		<div className="file">
			<div>
				<img src={icon} style={imgStyle} alt="???"/>	
			</div>
			<div>
				<span>{props.name}</span>
			</div>
		</div>
  	);
}

export default File;