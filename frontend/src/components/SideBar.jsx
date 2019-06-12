import React from 'react'

const SideBar = props => {

	const folderInfo = () => {
		if (props.folderInfo !== undefined) {
			return 	<div className="element-info">
						<div>
							<span className="description-text">Path</span>
							<span className="element-info-text">{props.folderInfo.path}</span>
						</div>
						<div>
							<span className="description-text">Name</span>
							<span className="element-info-text">{props.folderInfo.name}</span>
						</div>
						<div>
							<span className="description-text">Folders</span>
							<span className="element-info-text">{props.folders}</span>
						</div>
						<div>
							<span className="description-text">Files</span>
							<span className="element-info-text">{props.files}</span>
						</div>
						<div>
							<span className="description-text">Size</span>
							<span className="element-info-text">{props.folderInfo.size}</span>
						</div>
					</div>
		}
	};

	return (
		<div className="side-panel">
			<div className="side-panel-content">
				{folderInfo()}
				<div className="memory-info">
					<div className="used-memory-info">
						<span className="used-memory-text">
							{props.memory.total - props.memory.free} / {props.memory.total} GB
						</span>
					</div>
					<div className="total-memory-bar">
						<div 
							className="used-memory-bar"
							style={{ width : `${(props.memory.total - props.memory.free) / props.memory.total * 100}%` }}>
						</div>
					</div>
				</div>
			</div>
		</div>
	);
}

export default SideBar;