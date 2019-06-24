import React, { Component } from 'react';

interface RightPanelProps {
	closePanel: (event: React.MouseEvent<HTMLDivElement>) => void
}

export default class RightPanel extends Component<RightPanelProps> {

	render() {
		return (
			<div id="right-panel" className="right-panel">		
				<div className="right-panel-header">
					<span style={{ lineHeight : '50px' }}>Notifications</span>	
				</div>
				<div className="right-panel-close" onClick={this.props.closePanel}>
					<i className="fas fa-caret-right"/>
				</div>
				<div style={{
					marginTop: '50px',
					height: '70%'
				}}>
					{this.props.children}
				</div>
			</div>
		);
	}
}