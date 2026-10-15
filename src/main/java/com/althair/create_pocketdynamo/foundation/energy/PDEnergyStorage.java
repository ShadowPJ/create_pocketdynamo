package com.althair.create_pocketdynamo.foundation.energy;

import java.util.function.IntSupplier;

import net.minecraft.util.Mth;
import net.neoforged.neoforge.energy.IEnergyStorage;

/**
 * Small FE buffer with live limits (read from config each call, so config changes apply without a restart).
 * Owners call {@link #setOnChange(Runnable)} to mark their block entity dirty.
 */
public class PDEnergyStorage implements IEnergyStorage {
	private final IntSupplier capacity;
	private final IntSupplier maxReceive;
	private final IntSupplier maxExtract;
	private int energy;
	private Runnable onChange = () -> {
	};

	public PDEnergyStorage(IntSupplier capacity, IntSupplier maxReceive, IntSupplier maxExtract) {
		this.capacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
	}

	public void setOnChange(Runnable onChange) {
		this.onChange = onChange;
	}

	/** Directly set the stored amount (loading from NBT / item components). Clamped to capacity. */
	public void setEnergy(int value) {
		int clamped = Mth.clamp(value, 0, getMaxEnergyStored());
		if (clamped != energy) {
			energy = clamped;
			onChange.run();
		}
	}

	/** Add energy ignoring the receive limit (a generator filling its own buffer). Returns what was added. */
	public int generate(int amount) {
		int added = Math.min(Math.max(amount, 0), getMaxEnergyStored() - energy);
		if (added > 0) {
			energy += added;
			onChange.run();
		}
		return added;
	}

	/** Remove energy ignoring the extract limit (the motor draining its own battery). Returns what was removed. */
	public int drain(int amount) {
		int removed = Math.min(Math.max(amount, 0), energy);
		if (removed > 0) {
			energy -= removed;
			onChange.run();
		}
		return removed;
	}

	@Override
	public int receiveEnergy(int toReceive, boolean simulate) {
		if (!canReceive() || toReceive <= 0)
			return 0;
		int accepted = Math.min(Math.min(toReceive, maxReceive.getAsInt()), getMaxEnergyStored() - energy);
		if (accepted > 0 && !simulate) {
			energy += accepted;
			onChange.run();
		}
		return Math.max(accepted, 0);
	}

	@Override
	public int extractEnergy(int toExtract, boolean simulate) {
		if (!canExtract() || toExtract <= 0)
			return 0;
		int extracted = Math.min(Math.min(toExtract, maxExtract.getAsInt()), energy);
		if (extracted > 0 && !simulate) {
			energy -= extracted;
			onChange.run();
		}
		return extracted;
	}

	@Override
	public int getEnergyStored() {
		return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		return capacity.getAsInt();
	}

	@Override
	public boolean canExtract() {
		return maxExtract.getAsInt() > 0;
	}

	@Override
	public boolean canReceive() {
		return maxReceive.getAsInt() > 0;
	}
}
