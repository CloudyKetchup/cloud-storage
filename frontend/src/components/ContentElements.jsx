import React from 'react'

class ContentElements extends React.Component {
	render() {	
		return (
			<div className="elements">{this.props.children}</div>
		);
	}
};

export default ContentElements;