import React from 'react'

const SideBar = props => {
	const freeMemory  = props.memory.free;
	const totalMemory = props.memory.total;
	const usedMemory  = totalMemory - freeMemory;

	return (
		<div className="side-panel">
			<div className="side-panel-content">
				<div className="file-info">
				</div>
				<div className="memory-info">
					<div className="used-memory-info">
						<span className="used-memory-text">{usedMemory} / {totalMemory} GB</span>
					</div>
					<div className="total-memory-bar">
						<div 
							className="used-memory-bar"
							style={{ width : `${usedMemory / totalMemory * 100}%` }}>
						</div>
					</div>
				</div>
			</div>
		</div>
	);
}

export default SideBar