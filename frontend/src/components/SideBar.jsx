import React, {Component} from 'react'

class SideBar extends Component {

	render() {
		return (
			<div className="side-panel">
				<div className="side-panel-content">
					{
						this.props.folderInfo !== undefined
							? 	<div className="element-info">
									<div>
										<span className="description-text">Path</span>
										<span className="element-info-text">{this.props.folderInfo.path}</span>
									</div>
									<div>
										<span className="description-text">Name</span>
										<span className="element-info-text">{this.props.folderInfo.name}</span>
									</div>
									<div>
										<span className="description-text">Folders</span>
										<span className="element-info-text">{this.props.folders}</span>
									</div>
									<div>
										<span className="description-text">Files</span>
										<span className="element-info-text">{this.props.files}</span>
									</div>
									<div>
										<span className="description-text">Size</span>
										<span className="element-info-text">{this.props.folderInfo.size}</span>
									</div>
								</div>
							: undefined
					}
					<div className="memory-info">
						<div className="used-memory-info">
							<span className="used-memory-text">
								{this.props.memory.total - this.props.memory.free} / {this.props.memory.total} GB
							</span>
						</div>
						<div className="total-memory-bar">
							<div 
								className="used-memory-bar"
								style={{ width : `${(this.props.memory.total - this.props.memory.free) / this.props.memory.total * 100}%` }}>
							</div>
						</div>
					</div>
				</div>
			</div>
		);
	}
}

export default SideBar;