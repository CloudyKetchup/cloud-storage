import React from 'react'

const PrevFolderButton = props => {

	const style = {
		display : props.rootOpened ? 'none' : 'block',
		position : 'fixed',
		padding : '15px',
		paddingLeft : '10px'
	}

	return (
		<button className="prev-button" onClick={props.whenClicked} style={style}>
			<i className="fas fa-chevron-left"></i>
		</button>	
	);
};

export default PrevFolderButton;